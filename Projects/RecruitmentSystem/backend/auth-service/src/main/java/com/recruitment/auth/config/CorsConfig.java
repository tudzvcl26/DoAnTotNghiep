package com.recruitment.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String origins,
            @Value("${app.cors.allow-credentials:false}") boolean credentials,
            @Value("${app.cors.max-age-seconds:3600}") long maxAge) {
        CorsConfiguration cors = configured(origins, credentials, maxAge);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    static CorsConfiguration configured(String origins, boolean credentials, long maxAge) {
        List<String> allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (credentials && allowedOrigins.contains("*")) {
            throw new IllegalStateException("Wildcard CORS origin cannot be used with credentials.");
        }
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(allowedOrigins);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Correlation-Id"));
        cors.setExposedHeaders(List.of("Content-Disposition", "X-Correlation-Id"));
        cors.setAllowCredentials(credentials);
        cors.setMaxAge(maxAge);
        return cors;
    }
}
