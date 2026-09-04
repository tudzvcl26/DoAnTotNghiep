package com.recruitment.gateway.config;

import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class GatewayHttpClientConfig {
    @Bean
    HttpClientCustomizer boundedServiceDnsCache() {
        return client -> client.resolver(resolver -> resolver
                .cacheMinTimeToLive(Duration.ZERO)
                .cacheMaxTimeToLive(Duration.ofSeconds(5))
                .cacheNegativeTimeToLive(Duration.ZERO));
    }
}
