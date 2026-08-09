package com.recruitment.notification.amqp;

import com.recruitment.notification.config.NotificationAmqpProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationAmqpConfig {

    private final NotificationAmqpProperties properties;

    @Bean
    Declarables notificationAmqpTopology() {
        TopicExchange exchange = new TopicExchange(properties.getExchange(), true, false);
        TopicExchange deadLetterExchange = new TopicExchange(properties.getDeadLetterExchange(), true, false);
        Queue queue = new Queue(properties.getQueue(), true, false, false,
                java.util.Map.of("x-dead-letter-exchange", properties.getDeadLetterExchange()));
        Queue deadLetterQueue = new Queue(properties.getDeadLetterQueue(), true);
        Binding mainBinding = BindingBuilder.bind(queue).to(exchange).with("#");
        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
        return new Declarables(exchange, deadLetterExchange, queue, deadLetterQueue, mainBinding, deadLetterBinding);
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    ApplicationRunner notificationAmqpTopologyInitializer(RabbitAdmin rabbitAdmin) {
        return arguments -> rabbitAdmin.initialize();
    }

    @Bean
    MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }

}
