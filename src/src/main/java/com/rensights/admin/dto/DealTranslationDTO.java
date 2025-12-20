package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealTranslationDTO {
    private UUID id;
    private UUID dealId;
    private String languageCode;
    private String fieldName;
    private String translatedValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


