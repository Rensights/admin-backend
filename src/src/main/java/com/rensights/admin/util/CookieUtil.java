package com.rensights.admin.util;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * SECURITY: Utility class for managing secure HttpOnly cookies for admin JWT tokens.
 * HttpOnly cookies prevent JavaScript access, protecting against XSS attacks.
 */
@Component
public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);
    private static final String JWT_COOKIE_NAME = "adminAuthToken";

    @Value("${jwt.cookie.max-age:86400}") // Default: 24 hours in seconds
    private int cookieMaxAge;

    @Value("${jwt.cookie.secure:true}")
    private boolean cookieSecure; // Should be true in production (HTTPS only)

    @Value("${jwt.cookie.same-site:strict}")
    private String cookieSameSite; // strict, lax, or none

    @Value("${jwt.cookie.path:/}")
    private String cookiePath;

    @Value("${jwt.cookie.domain:}") // Empty = current domain (allows cross-port in dev)
    private String cookieDomain;

    /**
     * Set admin JWT token as HttpOnly cookie.
     * SECURITY: HttpOnly prevents JavaScript access (XSS protection).
     * SECURITY: Secure flag ensures cookie only sent over HTTPS (in production).
     * SECURITY: SameSite=Strict provides CSRF protection.
     */
    public void setAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(JWT_COOKIE_NAME, token)
                .path(cookiePath)
                .maxAge(cookieMaxAge)
                .httpOnly(true) // CRITICAL: Prevents JavaScript access (XSS protection)
                .secure(cookieSecure) // Only sent over HTTPS in production
                .sameSite(cookieSameSite); // CSRF protection

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader("Set-Cookie", cookie.toString());
        logger.debug("Set admin auth cookie: name={}, secure={}, sameSite={}, path={}, domain={}",
                JWT_COOKIE_NAME, cookieSecure, cookieSameSite, cookiePath,
                cookieDomain != null && !cookieDomain.isEmpty() ? cookieDomain : "default");
    }

    /**
     * Clear admin JWT cookie by setting it to expire immediately.
     * IMPORTANT: Must use the same domain, path, and secure/sameSite settings as when
     * the cookie was set, otherwise the browser will not clear it properly.
     */
    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(JWT_COOKIE_NAME, "")
                .path(cookiePath)
                .maxAge(0) // Expire immediately
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader("Set-Cookie", cookie.toString());
        logger.debug("Cleared admin auth cookie: name={}, path={}, domain={}",
                JWT_COOKIE_NAME, cookiePath,
                cookieDomain != null && !cookieDomain.isEmpty() ? cookieDomain : "default");
    }

    /**
     * Get cookie name for token extraction in the JWT filter.
     */
    public static String getCookieName() {
        return JWT_COOKIE_NAME;
    }
}
