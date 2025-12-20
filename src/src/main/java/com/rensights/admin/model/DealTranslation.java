package com.rensights.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deal_translations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"deal_id", "language_code", "field_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "deal_id", nullable = false)
    private UUID dealId; // Reference to Deal entity
    
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode; // e.g., "en", "ar", "fr"
    
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName; // e.g., "name", "location", "city", "area"
    
    @Column(name = "translated_value", nullable = false, columnDefinition = "TEXT")
    private String translatedValue; // The translated content
    
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


