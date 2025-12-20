package com.rensights.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "landing_page_content", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"section", "language_code", "field_key"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageContent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "section", nullable = false, length = 50)
    private String section; // e.g., "hero", "why-invest", "solutions", "how-it-works", "pricing", "footer"
    
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode; // e.g., "en", "ar", "fr"
    
    @Column(name = "field_key", nullable = false, length = 100)
    private String fieldKey; // e.g., "title", "subtitle", "description", "imageUrl", "videoUrl"
    
    @Column(name = "content_type", nullable = false, length = 20)
    @Builder.Default
    private String contentType = "text"; // "text", "image", "video", "json"
    
    @Column(name = "content_value", nullable = false, columnDefinition = "TEXT")
    private String contentValue; // The actual content (text, image URL, video URL, or JSON)
    
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0; // For ordering items in lists
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



