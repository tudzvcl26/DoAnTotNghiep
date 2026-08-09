package com.recruitment.notification.consumer;

import com.recruitment.notification.amqp.NotificationEventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.amqp", name = "consumer-enabled", havingValue = "true")
public class NotificationEventConsumer {

    private final NotificationEventHandler notificationEventHandler;
    private final NotificationFailureRecorder failureRecorder;

    @RabbitListener(queues = "${notification.amqp.queue}")
    public void consume(NotificationEventEnvelope event) {
        try {
            notificationEventHandler.handle(event);
        } catch (RuntimeException failure) {
            failureRecorder.record(event, failure);
            throw failure;
        }
    }

}
