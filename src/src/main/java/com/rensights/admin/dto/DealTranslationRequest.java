package com.rensights.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealTranslationRequest {
    @NotNull
    private UUID dealId;
    
    @NotBlank
    @Size(max = 10)
    private String languageCode;
    
    @NotBlank
    @Size(max = 50)
    private String fieldName; // e.g., "name", "location", "city", "area"
    
    @NotBlank
    private String translatedValue;
}



