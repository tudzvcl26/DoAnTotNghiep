package com.recruitment.application.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "application.outbox.publisher", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationOutboxPublisher {
    private final ApplicationOutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationOutboxProperties properties;

    @Scheduled(fixedDelayString = "${application.outbox.publisher.fixed-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        var events = repository.findByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
                OutboxStatus.PENDING, LocalDateTime.now(), PageRequest.of(0, properties.getBatchSize()));
        for (ApplicationOutboxEvent event : events) {
            publishOne(event);
        }
    }

    private void publishOne(ApplicationOutboxEvent event) {
        try {
            Message message = MessageBuilder.withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .setMessageId(event.getEventId().toString())
                    .setHeader("eventVersion", event.getEventVersion())
                    .build();
            CorrelationData correlation = new CorrelationData(event.getEventId().toString());
            rabbitTemplate.send(properties.getExchange(), event.getRoutingKey(), message, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ rejected event: " + confirm.getReason());
            }
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(LocalDateTime.now());
            event.setLastError(null);
        } catch (Exception exception) {
            int attempts = event.getAttempts() + 1;
            event.setAttempts(attempts);
            event.setLastError(abbreviate(exception.getMessage()));
            if (attempts >= properties.getMaxAttempts()) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("Outbox event {} exhausted retries", event.getEventId());
            } else {
                long delaySeconds = Math.min(300, 1L << Math.min(attempts, 8));
                event.setAvailableAt(LocalDateTime.now().plusSeconds(delaySeconds));
                log.warn("Outbox event {} publish attempt {} failed", event.getEventId(), attempts);
            }
        }
        repository.save(event);
    }

    private String abbreviate(String message) {
        if (message == null) {
            return "Unknown publisher error";
        }
        return message.length() <= 2000 ? message : message.substring(0, 2000);
    }
}
