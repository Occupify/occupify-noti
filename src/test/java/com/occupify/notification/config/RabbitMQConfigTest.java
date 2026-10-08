package com.occupify.notification.config;

import com.occupify.notification.dto.event.PasswordResetRequestedEvent;
import com.occupify.notification.dto.event.UserRegisteredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();
    private final MessageConverter converter = rabbitMQConfig.jackson2JsonMessageConverter();

    @Test
    @DisplayName("Should deserialize UserRegisteredEvent published from identity service")
    void shouldDeserializeIdentityUserRegisteredEvent() {
        UUID userId = UUID.randomUUID();
        String json = String.format("{\"userId\":\"%s\",\"email\":\"test@occupify.com\",\"otpCode\":\"123456\",\"occurredAt\":\"2026-10-07T03:00:00Z\"}", userId);

        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("__TypeId__", "com.occupify.identity.event.UserRegisteredEvent");

        Message message = new Message(json.getBytes(StandardCharsets.UTF_8), properties);

        Object result = converter.fromMessage(message);

        assertInstanceOf(UserRegisteredEvent.class, result);
        UserRegisteredEvent event = (UserRegisteredEvent) result;
        assertEquals(userId, event.userId());
        assertEquals("test@occupify.com", event.email());
        assertEquals("123456", event.otpCode());
    }

    @Test
    @DisplayName("Should deserialize PasswordResetRequestedEvent published from identity service")
    void shouldDeserializeIdentityPasswordResetRequestedEvent() {
        UUID userId = UUID.randomUUID();
        String json = String.format("{\"userId\":\"%s\",\"email\":\"reset@occupify.com\",\"otpCode\":\"654321\",\"occurredAt\":\"2026-10-07T03:00:00Z\"}", userId);

        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("__TypeId__", "com.occupify.identity.event.PasswordResetRequestedEvent");

        Message message = new Message(json.getBytes(StandardCharsets.UTF_8), properties);

        Object result = converter.fromMessage(message);

        assertInstanceOf(PasswordResetRequestedEvent.class, result);
        PasswordResetRequestedEvent event = (PasswordResetRequestedEvent) result;
        assertEquals(userId, event.userId());
        assertEquals("reset@occupify.com", event.email());
        assertEquals("654321", event.otpCode());
    }
}
