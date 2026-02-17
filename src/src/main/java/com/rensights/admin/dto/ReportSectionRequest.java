package com.rensights.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportSectionRequest {
    @NotBlank
    private String sectionKey;
    @NotBlank
    private String title;
    @NotBlank
    private String navTitle;
    private String description;
    @NotBlank
    private String accessTier;
    @NotNull
    private Integer displayOrder;
    @NotBlank
    private String languageCode;
    private Boolean isActive;
}
