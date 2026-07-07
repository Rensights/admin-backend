package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserLoginSummaryDTO {
    private long loginCount;
    private LocalDateTime lastLoginAt;
}
