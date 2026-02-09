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
public class SeedTranslationsRequest {
    @NotBlank
    private String sourceLanguageCode;

    @NotBlank
    private String targetLanguageCode;

    private boolean overwrite;
}
