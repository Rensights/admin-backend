package com.rensights.admin.service;

import com.rensights.admin.dto.DealTranslationDTO;
import com.rensights.admin.dto.DealTranslationRequest;
import com.rensights.admin.model.DealTranslation;
import com.rensights.admin.repository.DealTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealTranslationService {
    
    private final DealTranslationRepository dealTranslationRepository;
    
    @Transactional(readOnly = true)
    public List<DealTranslationDTO> getTranslationsByDeal(UUID dealId) {
        return dealTranslationRepository.findByDealId(dealId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DealTranslationDTO> getTranslationsByDealAndLanguage(UUID dealId, String languageCode) {
        return dealTranslationRepository.findByDealIdAndLanguageCode(dealId, languageCode).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<String, String> getTranslationMap(UUID dealId, String languageCode) {
        return dealTranslationRepository.findByDealIdAndLanguageCode(dealId, languageCode).stream()
            .collect(Collectors.toMap(
                DealTranslation::getFieldName,
                DealTranslation::getTranslatedValue
            ));
    }
    
    @Transactional
    public DealTranslationDTO createOrUpdateTranslation(DealTranslationRequest request) {
        DealTranslation translation = dealTranslationRepository
            .findByDealIdAndLanguageCodeAndFieldName(
                request.getDealId(),
                request.getLanguageCode(),
                request.getFieldName()
            )
            .orElse(DealTranslation.builder()
                .dealId(request.getDealId())
                .languageCode(request.getLanguageCode())
                .fieldName(request.getFieldName())
                .build()
            );
        
        translation.setTranslatedValue(request.getTranslatedValue());
        return toDTO(dealTranslationRepository.save(translation));
    }
    
    @Transactional
    public void deleteTranslation(UUID id) {
        dealTranslationRepository.deleteById(id);
    }
    
    @Transactional
    public void deleteTranslationsByDeal(UUID dealId) {
        dealTranslationRepository.deleteByDealId(dealId);
    }
    
    @Transactional
    public void deleteTranslationsByDealAndLanguage(UUID dealId, String languageCode) {
        dealTranslationRepository.deleteByDealIdAndLanguageCode(dealId, languageCode);
    }
    
    private DealTranslationDTO toDTO(DealTranslation translation) {
        return DealTranslationDTO.builder()
            .id(translation.getId())
            .dealId(translation.getDealId())
            .languageCode(translation.getLanguageCode())
            .fieldName(translation.getFieldName())
            .translatedValue(translation.getTranslatedValue())
            .createdAt(translation.getCreatedAt())
            .updatedAt(translation.getUpdatedAt())
            .build();
    }
}



