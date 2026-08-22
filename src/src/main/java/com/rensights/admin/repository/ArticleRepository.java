package com.rensights.admin.repository;

import com.rensights.admin.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Optional<Article> findById(UUID id);
    Optional<Article> findBySlug(String slug);
    Optional<Article> findBySlugAndIsActiveTrue(String slug);
    List<Article> findByIsActiveTrueOrderByPublishedAtDesc();
    List<Article> findAllByOrderByPublishedAtDesc();

    /** Articles carrying a given category — used to detach it before the category is deleted. */
    @org.springframework.data.jpa.repository.Query(
        "SELECT a FROM Article a JOIN a.categories c WHERE c.id = :categoryId")
    List<Article> findByCategoryId(
        @org.springframework.data.repository.query.Param("categoryId") UUID categoryId);
}
