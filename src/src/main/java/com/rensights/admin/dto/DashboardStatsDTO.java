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
    private long pendingAnalysisRequests;
    private java.util.List<MonthlyIncomeDTO> monthlyIncome;
    private java.util.List<DailyIncomeDTO> dailyIncome;
    private java.util.List<DeviceTypeStatDTO> deviceTypeStats;
    private java.util.List<MonthlyUserRegistrationsDTO> monthlyUserRegistrations;
    private java.util.List<DailyUserRegistrationsDTO> dailyUserRegistrations;
    private java.util.List<SubscriptionStatusStatDTO> subscriptionStatusStats;
}
