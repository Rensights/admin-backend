package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDocumentDTO {
    private String id;
    private String sectionId;
    private String title;
    private String description;
    private String filePath;
    private String originalFilename;
    private Long fileSize;
    private Integer displayOrder;
    private String languageCode;
    private Boolean isActive;
}
