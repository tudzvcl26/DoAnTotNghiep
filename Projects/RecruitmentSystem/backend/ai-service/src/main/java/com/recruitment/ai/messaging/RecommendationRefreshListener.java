package com.recruitment.ai.messaging;

import com.recruitment.ai.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationRefreshListener {
    private final RecommendationService recommendationService;

    @RabbitListener(queues = "${ai.messaging.recommendation-queue:ai.recommendation.refresh.q}",
            autoStartup = "${ai.messaging.recommendation-listener-enabled:true}")
    public void handle(RecommendationRefreshMessage message) {
        recommendationService.processJobRefresh(message);
    }
}
