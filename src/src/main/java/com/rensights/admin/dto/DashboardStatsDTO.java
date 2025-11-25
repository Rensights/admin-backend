package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalUsers;
    private long activeSubscriptions;
    private long totalRevenue;
    private long freeUsers;
    private long premiumUsers;
    private long enterpriseUsers;
    private long activeUsers;
    private long verifiedUsers;
}

