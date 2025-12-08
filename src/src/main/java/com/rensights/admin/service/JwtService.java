package com.rensights.admin.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class JwtService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret:admin-secret-key-change-in-production-minimum-32-characters-long}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    @javax.annotation.PostConstruct
    public void validateSecret() {
        // SECURITY: Validate JWT secret is strong enough (minimum 32 bytes for HS256)
        if (secret == null || secret.length() < 32) {
            logger.error("SECURITY ALERT: JWT secret is too short ({} chars). Minimum 32 characters required for HS256!", 
                secret != null ? secret.length() : 0);
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters long. Current length: " + 
                (secret != null ? secret.length() : 0) + 
                ". Please set JWT_SECRET environment variable with a strong secret (minimum 32 characters)."
            );
        }
        logger.info("JWT secret validated: length {} characters", secret.length());
    }

    private final AtomicReference<SecretKey> signingKey = new AtomicReference<>();

    private SecretKey getSigningKey() {
        if (signingKey.get() == null) {
            synchronized (this) {
                if (signingKey.get() == null) {
                    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                    signingKey.set(Keys.hmacShaKeyFor(keyBytes));
                }
            }
        }
        return signingKey.get();
    }

    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

