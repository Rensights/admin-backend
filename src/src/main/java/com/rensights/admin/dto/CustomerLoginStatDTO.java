package com.rensights.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerLoginStatDTO {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private long loginCount;
    private LocalDateTime lastLoginAt;
}
