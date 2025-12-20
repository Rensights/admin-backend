package com.rensights.admin.repository;

import com.rensights.admin.model.LandingPageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandingPageContentRepository extends JpaRepository<LandingPageContent, UUID> {
    
    // Find all content for a section
    List<LandingPageContent> findBySection(String section);
    
    // Find content for a section in a specific language
    List<LandingPageContent> findBySectionAndLanguageCode(String section, String languageCode);
    
    // Find active content for a section in a specific language
    List<LandingPageContent> findBySectionAndLanguageCodeAndIsActiveTrueOrderByDisplayOrderAsc(
        String section, String languageCode
    );
    
    // Find a specific field
    Optional<LandingPageContent> findBySectionAndLanguageCodeAndFieldKey(
        String section, String languageCode, String fieldKey
    );
    
    // Find all content for a language
    List<LandingPageContent> findByLanguageCode(String languageCode);
    
    // Delete all content for a section
    void deleteBySection(String section);
    
    // Delete content for a section in a specific language
    void deleteBySectionAndLanguageCode(String section, String languageCode);
}



