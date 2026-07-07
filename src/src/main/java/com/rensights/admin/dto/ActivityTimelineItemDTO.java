package com.rensights.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityTimelineItemDTO {
    /** "LOGIN", "PAGE_VIEW", or a business event type like "DEAL_VIEWED". */
    private String eventType;
    private String pagePath;
    private String metadata;
    private LocalDateTime occurredAt;
}
