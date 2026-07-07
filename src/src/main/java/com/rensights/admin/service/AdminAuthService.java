package com.rensights.admin.service;

import com.rensights.admin.model.AdminUser;
import com.rensights.admin.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);
    
    @Autowired
    private AdminUserRepository adminUserRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Transactional
    public AdminAuthResponse login(String email, String password) {
        AdminUser admin = adminUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        if (!admin.getIsActive()) {
            throw new RuntimeException("Admin account is deactivated");
        }
        
        String token = jwtService.generateToken(admin.getId(), admin.getEmail());
        
        return AdminAuthResponse.builder()
                .token(token)
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .isSuperAdmin(admin.getIsSuperAdmin())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AdminAuthResponse {
        private String token;
        private String email;
        private String firstName;
        private String lastName;
        private Boolean isSuperAdmin;
    }
}

