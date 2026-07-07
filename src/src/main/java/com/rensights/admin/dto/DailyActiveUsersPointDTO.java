package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyActiveUsersPointDTO {
    private String date;
    private long activeUsers;
}
