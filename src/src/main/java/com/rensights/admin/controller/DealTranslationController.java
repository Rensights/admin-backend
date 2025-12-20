package com.rensights.admin.controller;

import com.rensights.admin.dto.DealTranslationDTO;
import com.rensights.admin.dto.DealTranslationRequest;
import com.rensights.admin.service.DealTranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/deal-translations")
@RequiredArgsConstructor
public class DealTranslationController {
    
    private final DealTranslationService dealTranslationService;
    
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<List<DealTranslationDTO>> getTranslationsByDeal(@PathVariable UUID dealId) {
        return ResponseEntity.ok(dealTranslationService.getTranslationsByDeal(dealId));
    }
    
    @GetMapping("/deal/{dealId}/language/{languageCode}")
    public ResponseEntity<Map<String, String>> getTranslationMap(
        @PathVariable UUID dealId,
        @PathVariable String languageCode
    ) {
        return ResponseEntity.ok(dealTranslationService.getTranslationMap(dealId, languageCode));
    }
    
    @PostMapping
    public ResponseEntity<DealTranslationDTO> createOrUpdateTranslation(
        @Valid @RequestBody DealTranslationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(dealTranslationService.createOrUpdateTranslation(request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable UUID id) {
        dealTranslationService.deleteTranslation(id);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/deal/{dealId}")
    public ResponseEntity<Void> deleteTranslationsByDeal(@PathVariable UUID dealId) {
        dealTranslationService.deleteTranslationsByDeal(dealId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/deal/{dealId}/language/{languageCode}")
    public ResponseEntity<Void> deleteTranslationsByDealAndLanguage(
        @PathVariable UUID dealId,
        @PathVariable String languageCode
    ) {
        dealTranslationService.deleteTranslationsByDealAndLanguage(dealId, languageCode);
        return ResponseEntity.noContent().build();
    }
}


