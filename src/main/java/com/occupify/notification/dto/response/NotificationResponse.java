package com.occupify.notification.dto.response;

import com.occupify.notification.enums.NotificationCategory;
import com.occupify.notification.enums.NotificationTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private NotificationCategory category;
    private NotificationTemplate actionType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
