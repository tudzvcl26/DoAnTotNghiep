package com.recruitment.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.recommendations")
public class RecommendationProperties {
    private int candidatePoolSize = 50;
    private int topK = 3;
    private String cacheVersion = "v1";
    private long cacheTtlHours = 24;
}
