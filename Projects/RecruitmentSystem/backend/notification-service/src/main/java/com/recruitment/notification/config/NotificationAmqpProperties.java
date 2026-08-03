package com.recruitment.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "notification.amqp")
public class NotificationAmqpProperties {

    private boolean consumerEnabled;
    private String exchange;
    private String queue;
    private String deadLetterExchange;
    private String deadLetterQueue;

}
