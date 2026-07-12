package com.rensights.admin.service;

import com.rensights.admin.dto.ArticleDTO;
import com.rensights.admin.dto.ArticleRequest;
import com.rensights.admin.model.AppSetting;
import com.rensights.admin.model.Article;
import com.rensights.admin.repository.AppSettingRepository;
import com.rensights.admin.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    public static final String ARTICLES_ENABLED_KEY = "articles.enabled";

    // Matches the filename out of a self-hosted article image URL, e.g.
    // http://.../api/articles/images/3fa2b1c4-....jpg -> 3fa2b1c4-....jpg
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("/api/articles/images/([A-Za-z0-9._-]+)");

    // Matches a base64 image data URI embedded anywhere in a string (e.g. an
    // <img src="data:image/png;base64,...."> inside rich-text content). Base64
    // never contains '"', so the greedy run stops at the closing attribute quote.
    private static final Pattern CONTENT_DATA_URI_PATTERN =
        Pattern.compile("data:image/[A-Za-z0-9.+-]+;base64,[A-Za-z0-9+/=\\s]+");

    // Relative path served by app-backend's GET /api/articles/images/{filename};
    // app-frontend's resolveApiUrl() prefixes the API base for <img src>.
    private static final String IMAGE_URL_PREFIX = "/api/articles/images/";

    private final ArticleRepository articleRepository;
    private final AppSettingRepository appSettingRepository;
    private final ArticleImageStorageService articleImageStorageService;

    @Transactional(readOnly = true)
    public List<ArticleDTO> listPublic() {
        if (!isArticlesEnabled()) {
            return List.of();
        }
        return articleRepository.findByIsActiveTrueOrderByPublishedAtDesc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArticleDTO getPublicById(String id) {
        if (!isArticlesEnabled()) {
            return null;
        }
        return articleRepository.findById(UUID.fromString(id))
            .filter(article -> Boolean.TRUE.equals(article.getIsActive()))
            .map(this::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public ArticleDTO getPublicBySlug(String slug) {
        if (!isArticlesEnabled()) {
            return null;
        }
        return articleRepository.findBySlugAndIsActiveTrue(slug)
            .map(this::toDTO)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ArticleDTO> listAdmin() {
        return articleRepository.findAllByOrderByPublishedAtDesc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public ArticleDTO create(ArticleRequest request) {
        Article article = Article.builder()
            .title(request.getTitle())
            .slug(request.getSlug())
            .excerpt(normalizeSpaces(request.getExcerpt()))
            .content(persistContentImages(normalizeSpaces(request.getContent())))
            .coverImage(persistCoverImage(request.getCoverImage()))
            .publishedAt(request.getPublishedAt())
            .isActive(Optional.ofNullable(request.getIsActive()).orElse(true))
            .build();
        Article saved = articleRepository.save(article);
        return toDTO(saved);
    }

    @Transactional
    public ArticleDTO update(UUID id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article not found"));

        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug());
        article.setExcerpt(normalizeSpaces(request.getExcerpt()));
        article.setContent(persistContentImages(normalizeSpaces(request.getContent())));
        article.setCoverImage(persistCoverImage(request.getCoverImage()));
        article.setPublishedAt(request.getPublishedAt());
        if (request.getIsActive() != null) {
            article.setIsActive(request.getIsActive());
        }

        Article saved = articleRepository.save(article);
        return toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Article article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return;
        }
        Set<String> imageFilenames = extractImageFilenames(article);
        articleRepository.delete(article);
        for (String filename : imageFilenames) {
            articleImageStorageService.deleteImage(filename);
        }
    }

    /**
     * Replaces non-breaking spaces (both the U+00A0 character and the &nbsp;
     * HTML entity) with regular spaces. Pasted rich text uses NBSP between every
     * word, which stops the browser wrapping lines and makes the article overflow
     * the mobile viewport; normalising on save keeps the stored content clean.
     */
    private String normalizeSpaces(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\u00A0', ' ').replace("&nbsp;", " ");
    }

    /**
     * If the cover is a base64 data URI, persist it to the shared reports volume
     * and return its "/api/articles/images/{filename}" URL so the article row
     * stays small. Anything else (already a URL, null, blank) is returned as-is.
     */
    private String persistCoverImage(String coverImage) {
        if (coverImage == null || !coverImage.trim().startsWith("data:image/")) {
            return coverImage;
        }
        try {
            String filename = articleImageStorageService.storeDataUri(coverImage);
            if (filename != null) {
                return IMAGE_URL_PREFIX + filename;
            }
        } catch (IOException e) {
            // Keep the original so the save still succeeds; a follow-up edit can retry.
        }
        return coverImage;
    }

    /**
     * Replaces any base64 image data URIs embedded in rich-text content with
     * "/api/articles/images/{filename}" URLs backed by real files on the shared
     * volume, so inline images don't bloat every read of the article.
     */
    private String persistContentImages(String content) {
        if (content == null || !content.contains("data:image/")) {
            return content;
        }
        Matcher matcher = CONTENT_DATA_URI_PATTERN.matcher(content);
        StringBuilder rewritten = new StringBuilder();
        while (matcher.find()) {
            String replacement = matcher.group();
            try {
                String filename = articleImageStorageService.storeDataUri(matcher.group());
                if (filename != null) {
                    replacement = IMAGE_URL_PREFIX + filename;
                }
            } catch (IOException e) {
                // Keep the original data URI on failure rather than dropping the image.
            }
            matcher.appendReplacement(rewritten, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(rewritten);
        return rewritten.toString();
    }

    private Set<String> extractImageFilenames(Article article) {
        Set<String> filenames = new HashSet<>();
        String combined = (article.getCoverImage() != null ? article.getCoverImage() : "")
            + " " + (article.getContent() != null ? article.getContent() : "");
        Matcher matcher = IMAGE_URL_PATTERN.matcher(combined);
        while (matcher.find()) {
            filenames.add(matcher.group(1));
        }
        return filenames;
    }

    @Transactional
    public ArticleDTO setActive(UUID id, boolean active) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article not found"));
        article.setIsActive(active);
        return toDTO(articleRepository.save(article));
    }

    @Transactional
    public boolean setArticlesEnabled(boolean enabled) {
        AppSetting setting = appSettingRepository.findById(ARTICLES_ENABLED_KEY)
            .orElseGet(() -> AppSetting.builder().settingKey(ARTICLES_ENABLED_KEY).build());
        setting.setSettingValue(Boolean.toString(enabled));
        appSettingRepository.save(setting);
        return enabled;
    }

    @Transactional(readOnly = true)
    public boolean isArticlesEnabled() {
        return appSettingRepository.findById(ARTICLES_ENABLED_KEY)
            .map(setting -> Boolean.parseBoolean(setting.getSettingValue()))
            .orElse(true);
    }

    private ArticleDTO toDTO(Article article) {
        return ArticleDTO.builder()
            .id(article.getId().toString())
            .title(article.getTitle())
            .slug(article.getSlug())
            .excerpt(article.getExcerpt())
            .content(article.getContent())
            .coverImage(article.getCoverImage())
            .publishedAt(article.getPublishedAt())
            .isActive(Boolean.TRUE.equals(article.getIsActive()))
            .build();
    }
}
