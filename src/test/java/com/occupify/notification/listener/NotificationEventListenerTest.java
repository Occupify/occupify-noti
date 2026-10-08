package com.occupify.notification.listener;

import com.occupify.notification.dto.event.NotificationEventPayload;
import com.occupify.notification.enums.NotificationTemplate;
import com.occupify.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    @DisplayName("Should handle notification event via RabbitMQ listener")
    void shouldHandleNotificationEvent() {
        UUID userId = UUID.randomUUID();
        NotificationEventPayload payload = NotificationEventPayload.builder()
                .eventId(UUID.randomUUID())
                .recipientUserId(userId)
                .actionType(NotificationTemplate.JOB_MATCH_POSTED)
                .build();

        listener.handleNotificationEvent(payload);

        verify(notificationService).processNotificationEvent(payload);
    }
}
