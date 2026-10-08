package com.occupify.notification.service.impl;

import com.occupify.notification.dto.event.NotificationEventPayload;
import com.occupify.notification.service.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisherImpl implements NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:occupify.notification.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.default-routing-key:notification.push}")
    private String defaultRoutingKey;

    @Override
    public void publish(NotificationEventPayload payload) {
        String routingKey = defaultRoutingKey;
        if (payload.actionType() != null) {
            routingKey = "notification." + payload.actionType().name().toLowerCase();
        }
        publish(routingKey, payload);
    }

    @Override
    public void publish(String routingKey, NotificationEventPayload payload) {
        log.info("Publishing notification event [{}] with routingKey [{}] to exchange [{}]",
                payload.eventId(), routingKey, exchangeName);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);
    }
}
