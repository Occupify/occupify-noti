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

    @Value("${app.rabbitmq.user-exchange:occupify.user.exchange}")
    private String userExchangeName;

    @Value("${app.rabbitmq.user-routing-key:user.#}")
    private String userRoutingKey;

    @Value("${app.rabbitmq.contract-exchange:occupify.contract.exchange}")
    private String contractExchangeName;

    @Value("${app.rabbitmq.contract-queue:occupify.notification.contract.queue}")
    private String contractQueueName;

    @Value("${app.rabbitmq.contract-created-routing-key:contract.created}")
    private String contractCreatedRoutingKey;

    @Value("${app.rabbitmq.contract-accepted-routing-key:contract.accepted}")
    private String contractAcceptedRoutingKey;

    @Value("${app.rabbitmq.contract-rejected-routing-key:contract.rejected}")
    private String contractRejectedRoutingKey;

    @Value("${app.rabbitmq.contract-compensation-routing-key:contract.compensation.#}")
    private String contractCompensationRoutingKey;

    @Bean
    public TopicExchange contractExchange() {
        return new TopicExchange(contractExchangeName, true, false);
    }

    @Bean
    public Queue notiContractQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return QueueBuilder.durable(contractQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding notiContractCreatedBinding(Queue notiContractQueue, TopicExchange contractExchange) {
        return BindingBuilder.bind(notiContractQueue).to(contractExchange).with(contractCreatedRoutingKey);
    }

    @Bean
    public Binding notiContractAcceptedBinding(Queue notiContractQueue, TopicExchange contractExchange) {
        return BindingBuilder.bind(notiContractQueue).to(contractExchange).with(contractAcceptedRoutingKey);
    }

    @Bean
    public Binding notiContractRejectedBinding(Queue notiContractQueue, TopicExchange contractExchange) {
        return BindingBuilder.bind(notiContractQueue).to(contractExchange).with(contractRejectedRoutingKey);
    }

    @Bean
    public Binding notiContractCompensationBinding(Queue notiContractQueue, TopicExchange contractExchange) {
        return BindingBuilder.bind(notiContractQueue).to(contractExchange).with(contractCompensationRoutingKey);
    }

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
    public TopicExchange userExchange() {
        return new TopicExchange(userExchangeName, true, false);
    }

    @Bean
    public Binding userExchangeBinding(Queue userQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userQueue)
                .to(userExchange)
                .with(userRoutingKey);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper typeMapper =
                new org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.occupify.identity.event.UserRegisteredEvent", com.occupify.notification.dto.event.UserRegisteredEvent.class);
        idClassMapping.put("com.occupify.identity.event.UserActivatedEvent", com.occupify.notification.dto.event.UserActivatedEvent.class);
        idClassMapping.put("com.occupify.identity.event.PasswordResetRequestedEvent", com.occupify.notification.dto.event.PasswordResetRequestedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.UserRegisteredEvent", com.occupify.notification.dto.event.UserRegisteredEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.UserActivatedEvent", com.occupify.notification.dto.event.UserActivatedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.PasswordResetRequestedEvent", com.occupify.notification.dto.event.PasswordResetRequestedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.NotificationEventPayload", com.occupify.notification.dto.event.NotificationEventPayload.class);
        idClassMapping.put("UserRegisteredEvent", com.occupify.notification.dto.event.UserRegisteredEvent.class);
        idClassMapping.put("UserActivatedEvent", com.occupify.notification.dto.event.UserActivatedEvent.class);
        idClassMapping.put("PasswordResetRequestedEvent", com.occupify.notification.dto.event.PasswordResetRequestedEvent.class);
        idClassMapping.put("NotificationEventPayload", com.occupify.notification.dto.event.NotificationEventPayload.class);

        idClassMapping.put("com.occupify.core.event.ContractCreatedEvent", com.occupify.notification.dto.event.ContractCreatedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.ContractCreatedEvent", com.occupify.notification.dto.event.ContractCreatedEvent.class);
        idClassMapping.put("ContractCreatedEvent", com.occupify.notification.dto.event.ContractCreatedEvent.class);

        idClassMapping.put("com.occupify.core.event.ContractAcceptedEvent", com.occupify.notification.dto.event.ContractAcceptedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.ContractAcceptedEvent", com.occupify.notification.dto.event.ContractAcceptedEvent.class);
        idClassMapping.put("ContractAcceptedEvent", com.occupify.notification.dto.event.ContractAcceptedEvent.class);

        idClassMapping.put("com.occupify.core.event.ContractRejectedEvent", com.occupify.notification.dto.event.ContractRejectedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.ContractRejectedEvent", com.occupify.notification.dto.event.ContractRejectedEvent.class);
        idClassMapping.put("ContractRejectedEvent", com.occupify.notification.dto.event.ContractRejectedEvent.class);

        idClassMapping.put("com.occupify.core.event.ContractCompensationRequestedEvent", com.occupify.notification.dto.event.ContractCompensationRequestedEvent.class);
        idClassMapping.put("com.occupify.notification.dto.event.ContractCompensationRequestedEvent", com.occupify.notification.dto.event.ContractCompensationRequestedEvent.class);
        idClassMapping.put("ContractCompensationRequestedEvent", com.occupify.notification.dto.event.ContractCompensationRequestedEvent.class);

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
