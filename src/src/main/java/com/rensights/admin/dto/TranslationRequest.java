package com.rensights.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {
    @NotBlank(message = "Language code is required")
    private String languageCode;
    
    @NotBlank(message = "Namespace is required")
    private String namespace;
    
    @NotBlank(message = "Translation key is required")
    private String translationKey;
    
    @NotBlank(message = "Translation value is required")
    private String translationValue;
    
    private String description;
}



