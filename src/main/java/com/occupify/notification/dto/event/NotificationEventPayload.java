package com.occupify.notification.dto.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.occupify.notification.enums.NotificationCategory;
import com.occupify.notification.enums.NotificationTemplate;
import lombok.Builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationEventPayload(
    UUID eventId,
    UUID recipientUserId,
    String recipientEmail,
    String recipientFullName,
    String recipientAvatarUrl,
    NotificationCategory category,
    NotificationTemplate actionType,
    String customTitle,
    String customContent,
    List<Object> args,
    Map<String, String> metadata,
    Instant occurredAt
) implements Serializable {

    public NotificationEventPayload {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (category == null && actionType != null) {
            category = actionType.getCategory();
        }
    }
}
