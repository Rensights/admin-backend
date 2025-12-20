package com.rensights.admin.controller;

import com.rensights.admin.dto.LandingPageContentDTO;
import com.rensights.admin.dto.LandingPageContentRequest;
import com.rensights.admin.dto.LandingPageSectionDTO;
import com.rensights.admin.service.LandingPageContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/landing-page")
@RequiredArgsConstructor
public class LandingPageContentController {
    
    private final LandingPageContentService landingPageContentService;
    
    @GetMapping
    public ResponseEntity<List<LandingPageContentDTO>> getAllContent() {
        return ResponseEntity.ok(landingPageContentService.getAllContent());
    }
    
    @GetMapping("/section/{section}")
    public ResponseEntity<List<LandingPageContentDTO>> getContentBySection(@PathVariable String section) {
        return ResponseEntity.ok(landingPageContentService.getContentBySection(section));
    }
    
    @GetMapping("/section/{section}/language/{languageCode}")
    public ResponseEntity<LandingPageSectionDTO> getSectionContent(
        @PathVariable String section,
        @PathVariable String languageCode
    ) {
        return ResponseEntity.ok(landingPageContentService.getSectionContent(section, languageCode));
    }
    
    @GetMapping("/language/{languageCode}")
    public ResponseEntity<Map<String, LandingPageSectionDTO>> getAllSections(
        @PathVariable String languageCode
    ) {
        return ResponseEntity.ok(landingPageContentService.getAllSections(languageCode));
    }
    
    @PostMapping
    public ResponseEntity<LandingPageContentDTO> createOrUpdateContent(
        @Valid @RequestBody LandingPageContentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(landingPageContentService.createOrUpdateContent(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LandingPageContentDTO> updateContent(
        @PathVariable UUID id,
        @Valid @RequestBody LandingPageContentRequest request
    ) {
        return ResponseEntity.ok(landingPageContentService.updateContent(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable UUID id) {
        landingPageContentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/section/{section}/language/{languageCode}")
    public ResponseEntity<Void> deleteSectionContent(
        @PathVariable String section,
        @PathVariable String languageCode
    ) {
        landingPageContentService.deleteSectionContent(section, languageCode);
        return ResponseEntity.noContent().build();
    }
}



