package com.rensights.admin.repository;

import com.rensights.admin.model.DealTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealTranslationRepository extends JpaRepository<DealTranslation, UUID> {
    
    // Find all translations for a specific deal
    List<DealTranslation> findByDealId(UUID dealId);
    
    // Find translations for a deal in a specific language
    List<DealTranslation> findByDealIdAndLanguageCode(UUID dealId, String languageCode);
    
    // Find a specific field translation
    Optional<DealTranslation> findByDealIdAndLanguageCodeAndFieldName(
        UUID dealId, String languageCode, String fieldName
    );
    
    // Find all translations for a specific language across all deals
    List<DealTranslation> findByLanguageCode(String languageCode);
    
    // Delete all translations for a deal
    void deleteByDealId(UUID dealId);
    
    // Delete translations for a deal in a specific language
    void deleteByDealIdAndLanguageCode(UUID dealId, String languageCode);
}


