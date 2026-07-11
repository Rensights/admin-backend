package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyActiveUsersPointDTO {
    private String month; // "YYYY-MM"
    private long activeUsers;
}
