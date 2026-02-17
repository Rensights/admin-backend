package com.rensights.admin.repository;

import com.rensights.admin.model.ReportDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportDocumentRepository extends JpaRepository<ReportDocument, UUID> {
    List<ReportDocument> findBySectionIdOrderByDisplayOrderAsc(UUID sectionId);
    long countBySectionId(UUID sectionId);
}
