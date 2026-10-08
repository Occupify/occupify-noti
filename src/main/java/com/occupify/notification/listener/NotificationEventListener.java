package com.occupify.notification.listener;

import com.occupify.notification.dto.event.NotificationEventPayload;
import com.occupify.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbitmq.queue:occupify.notification.queue}")
    public void handleNotificationEvent(NotificationEventPayload event) {
        log.info("[Event Listener] Processing notification event [{}] for user: {}", event.eventId(), event.recipientUserId());
        notificationService.processNotificationEvent(event);
    }
}
