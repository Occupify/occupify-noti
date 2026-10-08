package com.occupify.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:occupify.notification.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue:occupify.notification.queue}")
    private String queueName;

    @Value("${app.rabbitmq.routing-key:notification.#}")
    private String routingKey;

    @Value("${app.rabbitmq.dlx-exchange:occupify.notification.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.dlq-queue:occupify.notification.dlq}")
    private String dlqQueueName;

    @Value("${app.rabbitmq.dlq-routing-key:notification.dlq}")
    private String dlqRoutingKey;

    @Value("${app.rabbitmq.user-queue:occupify.notification.user.queue}")
    private String userQueueName;

    @Value("${app.rabbitmq.user-routing-key:user.#}")
    private String userRoutingKey;

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return QueueBuilder.durable(queueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(routingKey);
    }

    @Bean
    public DirectExchange notificationDlxExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    @Bean
    public Binding notificationDlqBinding(Queue notificationDlq, DirectExchange notificationDlxExchange) {
        return BindingBuilder.bind(notificationDlq)
                .to(notificationDlxExchange)
                .with(dlqRoutingKey);
    }

    @Bean
    public Queue userQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return QueueBuilder.durable(userQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(userQueue)
                .to(notificationExchange)
                .with(userRoutingKey);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper typeMapper =
                new org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.occupify.identity.event.UserRegisteredEvent", com.occupify.notification.dto.event.UserRegisteredEvent.class);
        idClassMapping.put("com.occupify.identity.event.PasswordResetRequestedEvent", com.occupify.notification.dto.event.PasswordResetRequestedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.UserRegisteredEvent", com.occupify.notification.dto.event.UserRegisteredEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.PasswordResetRequestedEvent", com.occupify.notification.dto.event.PasswordResetRequestedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.NotificationEventPayload", com.occupify.notification.dto.event.NotificationEventPayload.class);
        idClassMapping.put("UserRegisteredEvent", com.occupify.notification.dto.event.UserRegisteredEvent.class);
        idClassMapping.put("PasswordResetRequestedEvent", com.occupify.notification.dto.event.PasswordResetRequestedEvent.class);
        idClassMapping.put("NotificationEventPayload", com.occupify.notification.dto.event.NotificationEventPayload.class);
        typeMapper.setIdClassMapping(idClassMapping);

        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
