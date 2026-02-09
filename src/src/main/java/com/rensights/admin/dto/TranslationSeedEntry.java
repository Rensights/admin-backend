package com.rensights.admin.dto;

import lombok.Data;

@Data
public class TranslationSeedEntry {
    private String namespace;
    private String translationKey;
    private String translationValue;
    private String description;
}
