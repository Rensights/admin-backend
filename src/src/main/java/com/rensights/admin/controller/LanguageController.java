package com.rensights.admin.controller;

import com.rensights.admin.dto.LanguageDTO;
import com.rensights.admin.dto.LanguageRequest;
import com.rensights.admin.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/languages")
@RequiredArgsConstructor
public class LanguageController {
    
    private final LanguageService languageService;
    
    @GetMapping
    public ResponseEntity<List<LanguageDTO>> getAllLanguages(Authentication authentication) {
        return ResponseEntity.ok(languageService.getAllLanguages());
    }
    
    @GetMapping("/{code}")
    public ResponseEntity<LanguageDTO> getLanguageByCode(@PathVariable String code, Authentication authentication) {
        return ResponseEntity.ok(languageService.getLanguageByCode(code));
    }
    
    @PostMapping
    public ResponseEntity<LanguageDTO> createLanguage(@Valid @RequestBody LanguageRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(languageService.createLanguage(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<LanguageDTO> updateLanguage(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(languageService.updateLanguage(id, request));
    }
    
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<LanguageDTO> toggleLanguage(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(languageService.toggleLanguage(id));
    }
    
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<LanguageDTO> setAsDefault(@PathVariable UUID id, Authentication authentication) {
        return ResponseEntity.ok(languageService.setAsDefault(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLanguage(@PathVariable UUID id, Authentication authentication) {
        languageService.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }
}

