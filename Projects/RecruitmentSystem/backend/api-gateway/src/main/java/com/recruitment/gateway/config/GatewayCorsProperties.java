package com.recruitment.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway.cors")
public record GatewayCorsProperties(List<String> allowedOrigins, boolean allowCredentials) {

    @PostConstruct
    void validate() {
        if (allowCredentials && allowedOrigins.contains("*")) {
            throw new IllegalStateException("CORS wildcard is not allowed when credentials are enabled");
        }
    }
}
