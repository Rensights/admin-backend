package com.rensights.admin.controller;

import com.rensights.admin.service.AdminAuthService;
import com.rensights.admin.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    private CookieUtil cookieUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        logger.info("Admin login attempt for: {}", request.getEmail());
        try {
            AdminAuthService.AdminAuthResponse authResponse = adminAuthService.login(
                    request.getEmail(),
                    request.getPassword()
            );
            // Set the JWT in an HttpOnly cookie — the token is NOT returned in the body
            cookieUtil.setAuthCookie(response, authResponse.getToken());
            logger.info("Admin login success for: {}", request.getEmail());
            return ResponseEntity.ok(new LoginResponseDTO(
                    authResponse.getEmail(),
                    authResponse.getFirstName(),
                    authResponse.getLastName(),
                    authResponse.getIsSuperAdmin()
            ));
        } catch (Exception e) {
            logger.error("Admin login error: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        cookieUtil.clearAuthCookie(response);
        logger.info("Admin logout: auth cookie cleared");
        return ResponseEntity.ok(new SuccessResponse("Logged out successfully"));
    }

    @lombok.Data
    private static class LoginRequest {
        private String email;
        private String password;
    }

    /** Response body for a successful login — does not include the JWT token. */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class LoginResponseDTO {
        private String email;
        private String firstName;
        private String lastName;
        private Boolean isSuperAdmin;
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
