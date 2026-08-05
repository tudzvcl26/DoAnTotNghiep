package com.recruitment.ai.observability;

import com.recruitment.ai.config.OllamaProperties;
import com.recruitment.ai.provider.OllamaStructuredGenerationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component("ollama")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai.provider", name = "type", havingValue = "OLLAMA")
public class OllamaHealthIndicator implements HealthIndicator {

    private final OllamaStructuredGenerationProvider provider;
    private final OllamaProperties properties;

    @Override
    public Health health() {
        OllamaStructuredGenerationProvider.OllamaAvailability availability = provider.availability();
        if (availability.online()) {
            return Health.up()
                    .withDetail("endpoint", properties.getBaseUrl())
                    .withDetail("model", properties.getModel())
                    .withDetail("modelAvailable", true)
                    .build();
        }
        return Health.down()
                .withDetail("endpoint", properties.getBaseUrl())
                .withDetail("model", properties.getModel())
                .withDetail("reachable", availability.reachable())
                .withDetail("modelAvailable", availability.modelAvailable())
                .build();
    }
}
