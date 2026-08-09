package com.recruitment.gateway.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gateway.services")
public record GatewayServiceProperties(
        @NotBlank String auth,
        @NotBlank String user,
        @NotBlank String company,
        @NotBlank String recruitment,
        @NotBlank String application,
        @NotBlank String notification,
        @NotBlank String ai) {
}
