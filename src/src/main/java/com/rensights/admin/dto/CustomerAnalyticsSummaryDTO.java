package com.rensights.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerAnalyticsSummaryDTO {
    private long dailyActiveUsers;
    private long monthlyActiveUsers;
    private long totalUsers;
}
