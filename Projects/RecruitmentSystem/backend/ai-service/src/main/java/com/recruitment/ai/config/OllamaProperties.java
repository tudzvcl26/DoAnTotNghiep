package com.recruitment.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.providers.ollama")
public class OllamaProperties {

    private boolean enabled;
    private String baseUrl;
    private String model;
    private double temperature;
    private double topP;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration timeout;

    public Duration getTimeout() {
        Duration maximum = Duration.ofSeconds(180);
        return timeout == null || timeout.isNegative() || timeout.isZero() || timeout.compareTo(maximum) > 0
                ? maximum : timeout;
    }

    public boolean isConfigured() {
        return enabled
                && baseUrl != null
                && !baseUrl.isBlank()
                && model != null
                && !model.isBlank();
    }

}
