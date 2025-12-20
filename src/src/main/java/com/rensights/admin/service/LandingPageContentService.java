package com.rensights.admin.service;

import com.rensights.admin.dto.LandingPageContentDTO;
import com.rensights.admin.dto.LandingPageContentRequest;
import com.rensights.admin.dto.LandingPageSectionDTO;
import com.rensights.admin.model.LandingPageContent;
import com.rensights.admin.repository.LandingPageContentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandingPageContentService {
    
    private final LandingPageContentRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional(readOnly = true)
    public List<LandingPageContentDTO> getAllContent() {
        return repository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LandingPageContentDTO> getContentBySection(String section) {
        return repository.findBySection(section).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public LandingPageSectionDTO getSectionContent(String section, String languageCode) {
        List<LandingPageContent> contents = repository
            .findBySectionAndLanguageCodeAndIsActiveTrueOrderByDisplayOrderAsc(section, languageCode);
        
        Map<String, Object> contentMap = new HashMap<>();
        for (LandingPageContent content : contents) {
            Object value = parseContentValue(content.getContentValue(), content.getContentType());
            contentMap.put(content.getFieldKey(), value);
        }
        
        return LandingPageSectionDTO.builder()
            .section(section)
            .languageCode(languageCode)
            .content(contentMap)
            .build();
    }
    
    @Transactional(readOnly = true)
    public Map<String, LandingPageSectionDTO> getAllSections(String languageCode) {
        List<String> sections = Arrays.asList("hero", "why-invest", "solutions", "how-it-works", "pricing", "footer");
        Map<String, LandingPageSectionDTO> result = new HashMap<>();
        
        for (String section : sections) {
            result.put(section, getSectionContent(section, languageCode));
        }
        
        return result;
    }
    
    @Transactional
    public LandingPageContentDTO createOrUpdateContent(LandingPageContentRequest request) {
        LandingPageContent content = repository
            .findBySectionAndLanguageCodeAndFieldKey(
                request.getSection(),
                request.getLanguageCode(),
                request.getFieldKey()
            )
            .orElse(LandingPageContent.builder()
                .section(request.getSection())
                .languageCode(request.getLanguageCode())
                .fieldKey(request.getFieldKey())
                .build()
            );
        
        content.setContentType(request.getContentType());
        content.setContentValue(request.getContentValue());
        content.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        content.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return toDTO(repository.save(content));
    }
    
    @Transactional
    public LandingPageContentDTO updateContent(UUID id, LandingPageContentRequest request) {
        LandingPageContent content = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        
        content.setSection(request.getSection());
        content.setLanguageCode(request.getLanguageCode());
        content.setFieldKey(request.getFieldKey());
        content.setContentType(request.getContentType());
        content.setContentValue(request.getContentValue());
        content.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        content.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        return toDTO(repository.save(content));
    }
    
    @Transactional
    public void deleteContent(UUID id) {
        repository.deleteById(id);
    }
    
    @Transactional
    public void deleteSectionContent(String section, String languageCode) {
        repository.deleteBySectionAndLanguageCode(section, languageCode);
    }
    
    private Object parseContentValue(String contentValue, String contentType) {
        if (contentValue == null) {
            return null;
        }
        
        switch (contentType.toLowerCase()) {
            case "json":
                try {
                    return objectMapper.readValue(contentValue, new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    return contentValue;
                }
            case "image":
            case "video":
            case "text":
            default:
                return contentValue;
        }
    }
    
    private LandingPageContentDTO toDTO(LandingPageContent content) {
        return LandingPageContentDTO.builder()
            .id(content.getId())
            .section(content.getSection())
            .languageCode(content.getLanguageCode())
            .fieldKey(content.getFieldKey())
            .contentType(content.getContentType())
            .contentValue(content.getContentValue())
            .displayOrder(content.getDisplayOrder())
            .isActive(content.getIsActive())
            .createdAt(content.getCreatedAt())
            .updatedAt(content.getUpdatedAt())
            .build();
    }
}


