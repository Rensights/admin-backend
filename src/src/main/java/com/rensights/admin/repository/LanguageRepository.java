package com.rensights.admin.repository;

import com.rensights.admin.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {
    
    Optional<Language> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Language> findByEnabledTrueOrderByNameAsc();
    
    List<Language> findAllByOrderByNameAsc();
    
    Optional<Language> findByIsDefaultTrue();
}


