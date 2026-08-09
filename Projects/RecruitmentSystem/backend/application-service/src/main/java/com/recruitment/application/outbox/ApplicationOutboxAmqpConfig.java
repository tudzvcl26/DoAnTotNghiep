package com.recruitment.application.outbox;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "application.outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationOutboxAmqpConfig {
    @Bean
    TopicExchange applicationEventExchange(ApplicationOutboxProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }
}
