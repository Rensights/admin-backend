package com.rensights.admin.repository;

import com.rensights.admin.model.ReportSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportSectionRepository extends JpaRepository<ReportSection, UUID> {
    List<ReportSection> findByLanguageCodeOrderByDisplayOrderAsc(String languageCode);
    Optional<ReportSection> findBySectionKeyAndLanguageCode(String sectionKey, String languageCode);
}
