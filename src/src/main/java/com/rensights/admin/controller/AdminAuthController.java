package com.rensights.admin.controller;

import com.rensights.admin.service.AdminAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);
    
    @Autowired
    private AdminAuthService adminAuthService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AdminAuthService.AdminAuthResponse response = adminAuthService.login(
                    request.getEmail(), 
                    request.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Admin login error: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/init-admin")
    public ResponseEntity<?> initAdmin() {
        try {
            adminAuthService.initializeDefaultAdmin();
            return ResponseEntity.ok(new SuccessResponse("Default admin user created successfully"));
        } catch (Exception e) {
            logger.error("Failed to initialize admin: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @lombok.Data
    private static class LoginRequest {
        private String email;
        private String password;
    }
    
    @lombok.Data
    private static class ErrorResponse {
        private String error;
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
    
    @lombok.Data
    private static class SuccessResponse {
        private String message;
        public SuccessResponse(String message) {
            this.message = message;
        }
    }
}

