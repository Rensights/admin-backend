package com.rensights.admin.config;

import com.rensights.admin.model.AdminUser;
import com.rensights.admin.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${admin.default-email:admin@rensights.com}")
    private String defaultAdminEmail;

    @Value("${admin.default-password:}")
    private String defaultAdminPassword;
    
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
            logger.info("Checking for existing admin user: {}", defaultAdminEmail);
            Optional<AdminUser> existingAdmin = adminUserRepository.findByEmail(defaultAdminEmail);

            if (existingAdmin.isEmpty()) {
                logger.info("═══════════════════════════════════════════════════════");
                logger.info("Creating default admin user...");

                AdminUser defaultAdmin = AdminUser.builder()
                        .email(defaultAdminEmail)
                        .passwordHash(passwordEncoder.encode(defaultAdminPassword))
                        .firstName("Admin")
                        .lastName("User")
                        .isActive(true)
                        .isSuperAdmin(true)
                        .build();

                adminUserRepository.save(defaultAdmin);

                logger.info("Default admin user created successfully!");
                logger.info("Email: {}", defaultAdminEmail);
                logger.warn("SECURITY: Please change the default password after first login!");
                logger.info("═══════════════════════════════════════════════════════");
            } else {
                logger.debug("Default admin user already exists - skipping creation");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize default admin user: {}", e.getMessage(), e);
        }
    }
}

