package com.rensights.admin.config;

import com.rensights.admin.config.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private RateLimitFilter rateLimitFilter;
    
    @Value("${cors.allowed-origins:http://localhost:3001,http://localhost:3002}")
    private String allowedOrigins;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // SECURITY NOTE: CSRF is disabled because we use stateless JWT authentication
            // Stateless REST APIs with JWT tokens are not vulnerable to traditional CSRF attacks
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // SECURITY FIX: Add security headers
            .headers(headers -> headers
                .contentTypeOptions(contentTypeOptions -> {})
                .frameOptions(frameOptions -> frameOptions.deny())
                .xssProtection(xss -> xss
                    .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true)
                    .preload(true)
                )
                .referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            )
            // SECURITY: Add filters in correct order
            // First add JWT filter before Spring Security's authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Then add rate limit filter before JWT filter (rate limiting happens first)
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/auth/**").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                .requestMatchers("/actuator/health").permitAll() // Only health endpoint public
                .requestMatchers("/actuator/**").authenticated() // Other actuator endpoints require auth
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Parse comma-separated origins from environment variable
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        
        // SECURITY FIX: Use allowedOriginPatterns instead of allowedOrigins for wildcard support
        // Build patterns list - include exact matches (no wildcard ports for security)
        List<String> patterns = new ArrayList<>();
        for (String origin : origins) {
            patterns.add(origin); // Add exact match first
            try {
                java.net.URL url = new java.net.URL(origin);
                String protocol = url.getProtocol();
                String host = url.getHost();
                int port = url.getPort();
                
                // SECURITY FIX: Removed wildcard port patterns to prevent attacks
                // Add pattern without port (defaults to 80 for http, 443 for https)
                String noPortPattern = protocol + "://" + host;
                if (!patterns.contains(noPortPattern)) {
                    patterns.add(noPortPattern);
                }
                
                // If there's an explicit port, also add pattern with that specific port
                if (port != -1) {
                    String specificPortPattern = protocol + "://" + host + ":" + port;
                    if (!patterns.contains(specificPortPattern)) {
                        patterns.add(specificPortPattern);
                    }
                }
            } catch (Exception e) {
                // If URL parsing fails, just use the origin as-is
            }
        }
        
        configuration.setAllowedOriginPatterns(patterns);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // SECURITY FIX: Restrict allowed headers instead of allowing all
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

