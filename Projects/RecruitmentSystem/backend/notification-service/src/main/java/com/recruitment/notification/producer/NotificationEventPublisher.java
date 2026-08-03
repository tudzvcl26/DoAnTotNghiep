package com.recruitment.notification.producer;

import com.recruitment.notification.amqp.NotificationEventEnvelope;

public interface NotificationEventPublisher {

    void publish(NotificationEventEnvelope event);

}
