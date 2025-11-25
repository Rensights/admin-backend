package com.rensights.admin.dto;

import com.rensights.admin.model.User;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private Boolean emailVerified;
    private User.UserTier userTier;
}

