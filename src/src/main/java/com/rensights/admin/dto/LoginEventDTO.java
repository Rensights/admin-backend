package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LoginEventDTO {
    private LocalDateTime loggedInAt;
    private String ipAddress;
}
