package com.recruitment.ai.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.messaging")
public class AiAmqpProperties {

    private boolean initializeTopology = true;
    private String inboundExchange;
    private String outboundExchange;
    private String queue;
    private String deadLetterExchange;
    private String deadLetterQueue;

}
