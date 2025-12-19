package com.rensights.admin.repository;

import com.rensights.admin.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, UUID> {
    
    List<Translation> findByLanguageCode(String languageCode);
    
    List<Translation> findByLanguageCodeAndNamespace(String languageCode, String namespace);
    
    Optional<Translation> findByLanguageCodeAndNamespaceAndTranslationKey(
        String languageCode, String namespace, String translationKey
    );
    
    List<String> findDistinctLanguageCode();
    
    List<String> findDistinctNamespaceByLanguageCode(String languageCode);
    
    boolean existsByLanguageCodeAndNamespaceAndTranslationKey(
        String languageCode, String namespace, String translationKey
    );
}

