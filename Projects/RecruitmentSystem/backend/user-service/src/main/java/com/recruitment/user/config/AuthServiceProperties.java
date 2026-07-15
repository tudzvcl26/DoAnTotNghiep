package com.recruitment.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth-service")
public class AuthServiceProperties {

    /**
     * Base URL của auth-service.
     * Ví dụ:
     * http://localhost:8081
     */
    private String baseUrl;

    /**
     * Endpoint xác thực JWT.
     * Ví dụ:
     * /api/v1/auth/validate
     */
    private String validateEndpoint = "/api/v1/auth/validate";

}