package com.rensights.admin.controller;

import com.rensights.admin.dto.ArticleCategoryDTO;
import com.rensights.admin.model.Article;
import com.rensights.admin.model.ArticleCategory;
import com.rensights.admin.repository.ArticleCategoryRepository;
import com.rensights.admin.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the categories articles are filed under, which become the filter pills on the public
 * Insights page.
 */
@RestController
@RequestMapping("/api/admin/article-categories")
public class ArticleCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleCategoryController.class);

    private final ArticleCategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public ArticleCategoryController(ArticleCategoryRepository categoryRepository,
                                     ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    @GetMapping
    public ResponseEntity<List<ArticleCategoryDTO>> list() {
        return ResponseEntity.ok(categoryRepository.findAllByOrderBySortOrderAscLabelAsc()
            .stream().map(this::toDTO).toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ArticleCategoryDTO request) {
        String label = request.getLabel() == null ? "" : request.getLabel().trim();
        if (label.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Label is required"));
        }

        String slug = slugify(request.getSlug() == null || request.getSlug().isBlank()
            ? label : request.getSlug());
        if (categoryRepository.findBySlug(slug).isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "A category with the key '" + slug + "' already exists"));
        }

        ArticleCategory saved = categoryRepository.save(ArticleCategory.builder()
            .slug(slug)
            .label(label)
            .color(request.getColor() == null || request.getColor().isBlank()
                ? "#B45309" : request.getColor().trim())
            .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
            .build());
        return ResponseEntity.ok(toDTO(saved));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> update(@PathVariable UUID categoryId,
                                    @RequestBody ArticleCategoryDTO request) {
        ArticleCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Category not found"));
        }

        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            category.setLabel(request.getLabel().trim());
        }
        if (request.getColor() != null && !request.getColor().isBlank()) {
            category.setColor(request.getColor().trim());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        // The slug is deliberately not editable: it is what /articles#market deep-links use, and
        // changing it would silently break any link already shared.
        return ResponseEntity.ok(toDTO(categoryRepository.save(category)));
    }

    /**
     * Delete a category, first detaching it from every article that uses it.
     *
     * <p>The join rows have to go explicitly — the association is owned by {@link Article}, so
     * deleting the category alone would trip the foreign key.
     */
    @DeleteMapping("/{categoryId}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable UUID categoryId) {
        ArticleCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return ResponseEntity.ok(Map.of("message", "Category deleted"));
        }

        List<Article> tagged = articleRepository.findByCategoryId(categoryId);
        for (Article article : tagged) {
            article.getCategories().removeIf(c -> c.getId().equals(categoryId));
        }
        articleRepository.saveAll(tagged);
        categoryRepository.delete(category);

        logger.info("Deleted article category {} (was on {} article(s))", categoryId, tagged.size());
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }

    /** URL/deep-link safe key: lowercase, spaces and punctuation collapsed to single dashes. */
    private String slugify(String value) {
        String slug = value.trim().toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+)|(-+$)", "");
        return slug.isEmpty() ? "category" : slug;
    }

    private ArticleCategoryDTO toDTO(ArticleCategory category) {
        return ArticleCategoryDTO.builder()
            .id(category.getId().toString())
            .slug(category.getSlug())
            .label(category.getLabel())
            .color(category.getColor())
            .sortOrder(category.getSortOrder())
            .build();
    }
}
