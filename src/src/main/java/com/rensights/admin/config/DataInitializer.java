package com.rensights.admin.config;

import com.rensights.admin.model.AdminUser;
import com.rensights.admin.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private AdminUserRepository adminUserRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Default admin credentials
    private static final String DEFAULT_ADMIN_EMAIL = "admin@rensights.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    
    @Override
    @Transactional
    public void run(String... args) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("DataInitializer.run() EXECUTING - Starting admin user initialization");
        logger.info("═══════════════════════════════════════════════════════");
        initializeDefaultAdmin();
    }
    
    @PostConstruct
    public void init() {
        logger.info("DataInitializer @PostConstruct called - Component is loaded");
    }
    
    private void initializeDefaultAdmin() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("DataInitializer.initializeDefaultAdmin() called - Starting admin user initialization");
        logger.info("═══════════════════════════════════════════════════════");
    
        try {
            logger.info("Checking for existing admin user: {}", DEFAULT_ADMIN_EMAIL);
            Optional<AdminUser> existingAdmin = adminUserRepository.findByEmail(DEFAULT_ADMIN_EMAIL);
            
            if (existingAdmin.isEmpty()) {
                logger.info("═══════════════════════════════════════════════════════");
                logger.info("Creating default admin user...");
                
                AdminUser defaultAdmin = AdminUser.builder()
                        .email(DEFAULT_ADMIN_EMAIL)
                        .passwordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                        .firstName("Admin")
                        .lastName("User")
                        .isActive(true)
                        .isSuperAdmin(true)
                        .build();
                
                adminUserRepository.save(defaultAdmin);
                
                logger.info("✅ Default admin user created successfully!");
                logger.info("📧 Email: {}", DEFAULT_ADMIN_EMAIL);
                logger.info("🔑 Password: {}", DEFAULT_ADMIN_PASSWORD);
                logger.warn("⚠️  SECURITY: Please change the default password after first login!");
                logger.info("═══════════════════════════════════════════════════════");
            } else {
                logger.debug("Default admin user already exists - skipping creation");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize default admin user: {}", e.getMessage(), e);
        }
    }
}

