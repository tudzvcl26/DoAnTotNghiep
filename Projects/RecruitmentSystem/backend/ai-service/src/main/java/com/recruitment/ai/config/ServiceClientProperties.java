package com.recruitment.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.clients")
public class ServiceClientProperties {
    private String recruitmentServiceUrl = "http://localhost:8084";
    private String companyServiceUrl = "http://localhost:8083";
    private String userServiceUrl = "http://localhost:8082";
    private String applicationServiceUrl = "http://localhost:8085";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
}
