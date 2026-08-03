package com.recruitment.notification.consumer;

import com.recruitment.notification.amqp.NotificationEventEnvelope;

public interface NotificationEventHandler {

    void handle(NotificationEventEnvelope event);

}
