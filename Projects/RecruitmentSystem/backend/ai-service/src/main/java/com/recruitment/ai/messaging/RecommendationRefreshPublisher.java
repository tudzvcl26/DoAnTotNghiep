package com.recruitment.ai.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationRefreshPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final AiAmqpProperties properties;

    public void publish(RecommendationRefreshMessage message) {
        rabbitTemplate.convertAndSend(properties.getOutboundExchange(), properties.getRecommendationRoutingKey(), message);
    }
}
