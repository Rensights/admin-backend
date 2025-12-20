package com.rensights.admin.service;

import com.rensights.admin.dto.LanguageDTO;
import com.rensights.admin.dto.LanguageRequest;
import com.rensights.admin.model.Language;
import com.rensights.admin.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageService {
    
    private final LanguageRepository languageRepository;
    
    @Transactional(readOnly = true)
    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAllByOrderByNameAsc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public LanguageDTO getLanguageByCode(String code) {
        return languageRepository.findByCode(code)
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("Language not found: " + code));
    }
    
    @Transactional
    public LanguageDTO createLanguage(LanguageRequest request) {
        if (languageRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Language with code " + request.getCode() + " already exists");
        }
        
        if (request.getIsDefault()) {
            languageRepository.findByIsDefaultTrue().ifPresent(existingDefault -> {
                existingDefault.setIsDefault(false);
                languageRepository.save(existingDefault);
            });
        }
        
        Language language = Language.builder()
            .code(request.getCode())
            .name(request.getName())
            .nativeName(request.getNativeName())
            .flag(request.getFlag())
            .enabled(request.getEnabled())
            .isDefault(request.getIsDefault())
            .build();
        
        language = languageRepository.save(language);
        return toDTO(language);
    }
    
    @Transactional
    public LanguageDTO updateLanguage(UUID id, LanguageRequest request) {
        Language language = languageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Language not found"));
        
        if (!language.getCode().equals(request.getCode())) {
            if (languageRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Language with code " + request.getCode() + " already exists");
            }
        }
        
        if (request.getIsDefault() && !language.getIsDefault()) {
            languageRepository.findByIsDefaultTrue().ifPresent(existingDefault -> {
                if (!existingDefault.getId().equals(id)) {
                    existingDefault.setIsDefault(false);
                    languageRepository.save(existingDefault);
                }
            });
        }
        
        language.setCode(request.getCode());
        language.setName(request.getName());
        language.setNativeName(request.getNativeName());
        language.setFlag(request.getFlag());
        language.setEnabled(request.getEnabled());
        language.setIsDefault(request.getIsDefault());
        
        language = languageRepository.save(language);
        return toDTO(language);
    }
    
    @Transactional
    public LanguageDTO toggleLanguage(UUID id) {
        Language language = languageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Language not found"));
        
        language.setEnabled(!language.getEnabled());
        
        if (!language.getEnabled() && language.getIsDefault()) {
            throw new RuntimeException("Cannot disable the default language");
        }
        
        language = languageRepository.save(language);
        return toDTO(language);
    }
    
    @Transactional
    public LanguageDTO setAsDefault(UUID id) {
        Language language = languageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Language not found"));
        
        if (!language.getEnabled()) {
            throw new RuntimeException("Cannot set a disabled language as default");
        }
        
        languageRepository.findByIsDefaultTrue().ifPresent(existingDefault -> {
            if (!existingDefault.getId().equals(id)) {
                existingDefault.setIsDefault(false);
                languageRepository.save(existingDefault);
            }
        });
        
        language.setIsDefault(true);
        language = languageRepository.save(language);
        return toDTO(language);
    }
    
    @Transactional
    public void deleteLanguage(UUID id) {
        Language language = languageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Language not found"));
        
        if (language.getIsDefault()) {
            throw new RuntimeException("Cannot delete the default language");
        }
        
        languageRepository.deleteById(id);
    }
    
    private LanguageDTO toDTO(Language language) {
        return LanguageDTO.builder()
            .id(language.getId())
            .code(language.getCode())
            .name(language.getName())
            .nativeName(language.getNativeName())
            .flag(language.getFlag())
            .enabled(language.getEnabled())
            .isDefault(language.getIsDefault())
            .createdAt(language.getCreatedAt())
            .updatedAt(language.getUpdatedAt())
            .build();
    }
}


