package com.rensights.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleRequest {
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImage;
    private LocalDateTime publishedAt;
    private Boolean isActive;
    /** Category ids to file this article under; null leaves the existing set untouched. */
    private java.util.List<String> categoryIds;
}
