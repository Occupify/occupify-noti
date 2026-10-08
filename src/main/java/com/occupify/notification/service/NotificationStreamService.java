package com.occupify.notification.service;

import com.occupify.notification.dto.response.NotificationResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface NotificationStreamService {

    SseEmitter subscribe(UUID userId);

    void sendNotification(UUID userId, NotificationResponse notification);
}
