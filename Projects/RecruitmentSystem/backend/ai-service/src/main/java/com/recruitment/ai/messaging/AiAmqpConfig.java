package com.recruitment.ai.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class AiAmqpConfig {

    private static final List<String> INBOUND_ROUTING_KEYS = List.of(
            "application.submitted",
            "application.withdrawn",
            "job.published",
            "job.closed",
            "profile.updated",
            "profile.asset.updated"
    );

    @Bean
    Declarables aiAmqpTopology(AiAmqpProperties properties) {
        TopicExchange inboundExchange = new TopicExchange(properties.getInboundExchange(), true, false);
        TopicExchange outboundExchange = new TopicExchange(properties.getOutboundExchange(), true, false);
        TopicExchange deadLetterExchange = new TopicExchange(properties.getDeadLetterExchange(), true, false);
        Queue queue = new Queue(
                properties.getQueue(),
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", properties.getDeadLetterExchange(),
                        "x-dead-letter-routing-key", properties.getQueue()
                )
        );
        Queue deadLetterQueue = new Queue(properties.getDeadLetterQueue(), true);

        List<Declarable> declarables = new ArrayList<>();
        declarables.add(inboundExchange);
        declarables.add(outboundExchange);
        declarables.add(deadLetterExchange);
        declarables.add(queue);
        declarables.add(deadLetterQueue);
        INBOUND_ROUTING_KEYS.forEach(key ->
                declarables.add(BindingBuilder.bind(queue).to(inboundExchange).with(key)));
        declarables.add(BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(properties.getQueue()));
        return new Declarables(declarables);
    }

    @Bean
    RabbitAdmin aiRabbitAdmin(ConnectionFactory connectionFactory, AiAmqpProperties properties) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(properties.isInitializeTopology());
        return rabbitAdmin;
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "ai.messaging",
            name = "initialize-topology",
            havingValue = "true",
            matchIfMissing = true
    )
    ApplicationRunner aiAmqpTopologyInitializer(RabbitAdmin aiRabbitAdmin) {
        return arguments -> aiRabbitAdmin.initialize();
    }

    @Bean
    MessageConverter aiRabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

}
