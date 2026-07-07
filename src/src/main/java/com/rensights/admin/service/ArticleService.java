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
            .excerpt(request.getExcerpt())
            .content(request.getContent())
            .coverImage(request.getCoverImage())
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
        article.setExcerpt(request.getExcerpt());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
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
