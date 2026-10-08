package com.occupify.notification.service;

import com.occupify.notification.dto.event.NotificationEventPayload;

public interface NotificationEventPublisher {

    void publish(NotificationEventPayload payload);

    void publish(String routingKey, NotificationEventPayload payload);
}
