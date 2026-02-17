package com.rensights.admin.controller;

import com.rensights.admin.dto.ReportDocumentDTO;
import com.rensights.admin.dto.ReportDocumentRequest;
import com.rensights.admin.dto.ReportSectionDTO;
import com.rensights.admin.dto.ReportSectionRequest;
import com.rensights.admin.service.ReportSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportSectionController {

    private static final Logger logger = LoggerFactory.getLogger(ReportSectionController.class);
    private final ReportSectionService reportSectionService;

    @GetMapping("/sections")
    public ResponseEntity<List<ReportSectionDTO>> getSections(@RequestParam(defaultValue = "en") String lang) {
        return ResponseEntity.ok(reportSectionService.getSections(lang));
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<ReportSectionDTO> getSection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(reportSectionService.getSection(sectionId));
    }

    @PostMapping("/sections")
    public ResponseEntity<ReportSectionDTO> createSection(@Valid @RequestBody ReportSectionRequest request) {
        return ResponseEntity.ok(reportSectionService.createSection(request));
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<ReportSectionDTO> updateSection(@PathVariable UUID sectionId, @Valid @RequestBody ReportSectionRequest request) {
        return ResponseEntity.ok(reportSectionService.updateSection(sectionId, request));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID sectionId) {
        reportSectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sections/{sectionId}/documents")
    public ResponseEntity<ReportDocumentDTO> uploadDocument(
        @PathVariable UUID sectionId,
        @RequestPart("file") MultipartFile file,
        @Valid @RequestPart("metadata") ReportDocumentRequest request) throws Exception {
        return ResponseEntity.ok(reportSectionService.uploadDocument(sectionId, request, file));
    }

    @PutMapping("/documents/{documentId}")
    public ResponseEntity<ReportDocumentDTO> updateDocument(@PathVariable UUID documentId, @Valid @RequestBody ReportDocumentRequest request) {
        return ResponseEntity.ok(reportSectionService.updateDocument(documentId, request));
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        reportSectionService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
