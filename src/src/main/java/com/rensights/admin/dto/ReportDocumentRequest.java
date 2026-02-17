package com.rensights.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDocumentRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Integer displayOrder;
    @NotBlank
    private String languageCode;
    private Boolean isActive;
}
