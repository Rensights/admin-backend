package com.rensights.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String budget;
    private String portfolio;
    private java.util.List<String> goals;
    private String registrationPlan;
    private String userTier;
    private String customerId;
    private String createdAt;
    private Boolean isActive;
    private Boolean emailVerified;
}
