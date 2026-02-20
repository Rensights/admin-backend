package com.rensights.admin.service;

import com.rensights.admin.dto.ReportDocumentDTO;
import com.rensights.admin.dto.ReportDocumentRequest;
import com.rensights.admin.dto.ReportSectionDTO;
import com.rensights.admin.dto.ReportSectionRequest;
import com.rensights.admin.model.ReportDocument;
import com.rensights.admin.model.ReportSection;
import com.rensights.admin.repository.ReportDocumentRepository;
import com.rensights.admin.repository.ReportSectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportSectionService {

    private final ReportSectionRepository sectionRepository;
    private final ReportDocumentRepository documentRepository;
    private final ReportStorageService storageService;

    public List<ReportSectionDTO> getSections(String languageCode) {
        return sectionRepository.findByLanguageCodeOrderByDisplayOrderAsc(languageCode)
            .stream()
            .map(this::toSectionDTO)
            .collect(Collectors.toList());
    }

    public ReportSectionDTO getSection(UUID id) {
        ReportSection section = sectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Section not found"));
        return toSectionDTO(section);
    }

    @Transactional
    public ReportSectionDTO createSection(ReportSectionRequest request) {
        ReportSection section = ReportSection.builder()
            .sectionKey(request.getSectionKey())
            .title(request.getTitle())
            .navTitle(request.getNavTitle())
            .description(request.getDescription())
            .accessTier(ReportSection.AccessTier.valueOf(request.getAccessTier().toUpperCase()))
            .displayOrder(request.getDisplayOrder())
            .languageCode(request.getLanguageCode())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();
        section = sectionRepository.save(section);
        return toSectionDTO(section);
    }

    @Transactional
    public ReportSectionDTO updateSection(UUID id, ReportSectionRequest request) {
        ReportSection section = sectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Section not found"));

        section.setTitle(request.getTitle());
        section.setNavTitle(request.getNavTitle());
        section.setDescription(request.getDescription());
        section.setLanguageCode(request.getLanguageCode());
        if (request.getIsActive() != null) {
            section.setIsActive(request.getIsActive());
        }
        section = sectionRepository.save(section);
        return toSectionDTO(section);
    }

    @Transactional
    public void deleteSection(UUID id) {
        List<ReportDocument> docs = documentRepository.findBySectionIdOrderByDisplayOrderAsc(id);
        docs.forEach(doc -> {
            if (doc.getFilePath() != null) {
                storageService.deleteFile(doc.getFilePath());
            }
        });
        documentRepository.deleteAll(docs);
        sectionRepository.deleteById(id);
    }

    @Transactional
    public ReportDocumentDTO uploadDocument(UUID sectionId, ReportDocumentRequest request, MultipartFile file) throws IOException {
        ReportSection section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Section not found"));

        long docCount = documentRepository.countBySectionId(sectionId);
        int maxDocs = section.getDisplayOrder() != null && section.getDisplayOrder() == 5 ? 8 : 1;
        if (docCount >= maxDocs) {
            throw new RuntimeException("Section already has maximum allowed documents");
        }

        String fileBase64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
        ReportDocument document = ReportDocument.builder()
            .section(section)
            .title(request.getTitle())
            .description(request.getDescription())
            .displayOrder(request.getDisplayOrder())
            .languageCode("en")
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .filePath(null)
            .fileContentBase64(fileBase64)
            .originalFilename(file.getOriginalFilename())
            .fileSize(file.getSize())
            .build();

        document = documentRepository.save(document);
        return toDocumentDTO(document);
    }

    @Transactional
    public ReportDocumentDTO updateDocument(UUID documentId, ReportDocumentRequest request) {
        ReportDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) {
            document.setIsActive(request.getIsActive());
        }
        document = documentRepository.save(document);
        return toDocumentDTO(document);
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        ReportDocument document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));
        if (document.getFilePath() != null && !document.getFilePath().isBlank()) {
            storageService.deleteFile(document.getFilePath());
        }
        documentRepository.delete(document);
    }

    private ReportSectionDTO toSectionDTO(ReportSection section) {
        List<ReportDocumentDTO> docs = documentRepository.findBySectionIdOrderByDisplayOrderAsc(section.getId())
            .stream()
            .map(this::toDocumentDTO)
            .collect(Collectors.toList());
        return ReportSectionDTO.builder()
            .id(section.getId().toString())
            .sectionKey(section.getSectionKey())
            .title(section.getTitle())
            .navTitle(section.getNavTitle())
            .description(section.getDescription())
            .accessTier(section.getAccessTier().name())
            .displayOrder(section.getDisplayOrder())
            .languageCode(section.getLanguageCode())
            .isActive(section.getIsActive())
            .documents(docs)
            .build();
    }

    private ReportDocumentDTO toDocumentDTO(ReportDocument doc) {
        return ReportDocumentDTO.builder()
            .id(doc.getId().toString())
            .sectionId(doc.getSection().getId().toString())
            .title(doc.getTitle())
            .description(doc.getDescription())
            .filePath(doc.getFilePath())
            .originalFilename(doc.getOriginalFilename())
            .fileSize(doc.getFileSize())
            .displayOrder(doc.getDisplayOrder())
            .languageCode(doc.getLanguageCode())
            .isActive(doc.getIsActive())
            .build();
    }
}
