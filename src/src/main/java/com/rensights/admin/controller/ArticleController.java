package com.rensights.admin.controller;

import com.rensights.admin.dto.ArticleDTO;
import com.rensights.admin.dto.ArticleRequest;
import com.rensights.admin.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // Public endpoints (for app-backend to consume)
    @GetMapping("/articles")
    public ResponseEntity<List<ArticleDTO>> listPublic() {
        List<ArticleDTO> articles = articleService.listPublic();
        if (articles.isEmpty() && !articleService.isArticlesEnabled()) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleDTO> getPublic(@PathVariable String id) {
        ArticleDTO article = articleService.getPublicById(id);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    @GetMapping("/articles/slug/{slug}")
    public ResponseEntity<ArticleDTO> getPublicBySlug(@PathVariable String slug) {
        ArticleDTO article = articleService.getPublicBySlug(slug);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    // Admin endpoints
    @GetMapping("/admin/articles")
    public ResponseEntity<List<ArticleDTO>> listAdmin() {
        return ResponseEntity.ok(articleService.listAdmin());
    }

    @PostMapping("/admin/articles/create")
    public ResponseEntity<ArticleDTO> create(@RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.create(request));
    }

    @PutMapping("/admin/articles/update/{id}")
    public ResponseEntity<ArticleDTO> update(@PathVariable UUID id, @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.update(id, request));
    }

    @DeleteMapping("/admin/articles/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/articles/enable")
    public ResponseEntity<?> enableAll(@RequestParam boolean enabled) {
        return ResponseEntity.ok(java.util.Map.of("enabled", articleService.setArticlesEnabled(enabled)));
    }

    @GetMapping("/admin/articles/enable")
    public ResponseEntity<?> getEnabled() {
        return ResponseEntity.ok(java.util.Map.of("enabled", articleService.isArticlesEnabled()));
    }

    @PutMapping("/admin/articles/enable/{id}")
    public ResponseEntity<ArticleDTO> enableOne(@PathVariable UUID id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(articleService.setActive(id, enabled));
    }
}
