package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageViewStatDTO {
    private String pagePath;
    private long viewCount;
}
