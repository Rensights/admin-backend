package com.rensights.admin.service;

import com.rensights.admin.model.AdminUser;
import com.rensights.admin.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

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
    
    @Transactional
    public void initializeDefaultAdmin() {
        String DEFAULT_ADMIN_EMAIL = "admin@rensights.com";
        String DEFAULT_ADMIN_PASSWORD = "admin123";
        
        Optional<AdminUser> existingAdmin = adminUserRepository.findByEmail(DEFAULT_ADMIN_EMAIL);
        
        if (existingAdmin.isEmpty()) {
            logger.info("Creating default admin user...");
            
            AdminUser defaultAdmin = AdminUser.builder()
                    .email(DEFAULT_ADMIN_EMAIL)
                    .passwordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                    .firstName("Admin")
                    .lastName("User")
                    .isActive(true)
                    .isSuperAdmin(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            adminUserRepository.save(defaultAdmin);
            logger.info("Default admin user created successfully!");
        } else {
            logger.info("Default admin user already exists");
        }
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

