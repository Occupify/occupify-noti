package com.occupify.notification.service.impl;

import com.occupify.notification.dto.response.NotificationResponse;
import com.occupify.notification.service.NotificationStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class NotificationStreamServiceImpl implements NotificationStreamService {

    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        try {
            // Send initial connection event
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to notification stream for user: " + userId));
            log.info("SSE client connected for user [{}]", userId);
        } catch (IOException e) {
            log.error("Failed to send initial SSE event to user [{}]: {}", userId, e.getMessage());
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    @Override
    public void sendNotification(UUID userId, NotificationResponse notification) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("No active SSE connections for user [{}]", userId);
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                String notiIdStr = notification.getId() != null ? notification.getId().toString() : "";
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .id(notiIdStr)
                        .data(notification));
                log.info("Pushed real-time notification [{}] to user [{}]", notiIdStr, userId);

            } catch (IOException e) {
                log.warn("Failed to push SSE to user [{}], removing stale emitter", userId);
                removeEmitter(userId, emitter);
            }
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
        log.debug("Removed SSE emitter for user [{}]", userId);
    }
}
