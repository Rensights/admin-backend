package com.rensights.admin.controller;

import com.rensights.admin.dto.TranslationDTO;
import com.rensights.admin.dto.TranslationRequest;
import com.rensights.admin.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/translations")
@RequiredArgsConstructor
public class TranslationController {
    
    private final TranslationService translationService;
    
    @GetMapping
    public ResponseEntity<List<TranslationDTO>> getAllTranslations(Authentication authentication) {
        return ResponseEntity.ok(translationService.getAllTranslations());
    }
    
    @GetMapping("/language/{languageCode}")
    public ResponseEntity<List<TranslationDTO>> getTranslationsByLanguage(
        @PathVariable String languageCode,
        Authentication authentication
    ) {
        return ResponseEntity.ok(translationService.getTranslationsByLanguage(languageCode));
    }
    
    @PostMapping
    public ResponseEntity<TranslationDTO> createTranslation(@Valid @RequestBody TranslationRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(translationService.createTranslation(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TranslationDTO> updateTranslation(
        @PathVariable UUID id,
        @Valid @RequestBody TranslationRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(translationService.updateTranslation(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable UUID id, Authentication authentication) {
        translationService.deleteTranslation(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getAvailableLanguages(Authentication authentication) {
        return ResponseEntity.ok(translationService.getAvailableLanguages());
    }
    
    @GetMapping("/language/{languageCode}/namespaces")
    public ResponseEntity<List<String>> getNamespaces(@PathVariable String languageCode, Authentication authentication) {
        return ResponseEntity.ok(translationService.getNamespaces(languageCode));
    }
}

