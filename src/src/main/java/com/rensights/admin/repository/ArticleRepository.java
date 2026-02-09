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
}
