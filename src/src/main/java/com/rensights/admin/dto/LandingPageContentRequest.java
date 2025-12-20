package com.rensights.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageContentRequest {
    @NotBlank
    @Size(max = 50)
    private String section;
    
    @NotBlank
    @Size(max = 10)
    private String languageCode;
    
    @NotBlank
    @Size(max = 100)
    private String fieldKey;
    
    @NotBlank
    @Size(max = 20)
    private String contentType; // "text", "image", "video", "json"
    
    @NotBlank
    private String contentValue;
    
    private Integer displayOrder;
    
    @NotNull
    private Boolean isActive;
}



