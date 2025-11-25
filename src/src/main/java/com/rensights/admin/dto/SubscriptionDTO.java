package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private String id;
    private String userId;
    private String userEmail;
    private String planType;
    private String status;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String stripeSubscriptionId;
}

