package com.recruitment.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.providers.openai")
public class OpenAiProperties {

    private boolean enabled;
    private String apiKey;
    private boolean apiKeyRequired = true;
    private String baseUrl;
    private String structuredModel;
    private String embeddingModel;
    private Duration connectTimeout;
    private Duration readTimeout;

    public Duration getReadTimeout() {
        Duration maximum = Duration.ofSeconds(180);
        return readTimeout == null || readTimeout.isNegative() || readTimeout.isZero() || readTimeout.compareTo(maximum) > 0
                ? maximum : readTimeout;
    }

    public boolean isConfigured() {
        return enabled
                && baseUrl != null
                && !baseUrl.isBlank()
                && (!apiKeyRequired || (apiKey != null && !apiKey.isBlank()));
    }

}
