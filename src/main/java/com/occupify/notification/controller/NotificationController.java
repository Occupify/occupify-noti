package com.occupify.notification.controller;

import com.occupify.notification.annotation.Authenticated;
import com.occupify.notification.dto.base.PageResponse;
import com.occupify.notification.dto.base.SingleResponse;
import com.occupify.notification.dto.base.SuccessResponse;
import com.occupify.notification.dto.response.NotificationResponse;
import com.occupify.notification.dto.response.UnreadCountResponse;
import com.occupify.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({ "/notifications" })
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController extends AbstractBaseController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications", description = "Get paginated notifications with unread count")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @Authenticated UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("[GET /notifications] Getting notifications for user: {}, page: {}, limit: {}", userId, page, limit);

        PageResponse<NotificationResponse> response = notificationService.getNotifications(userId, page, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<SingleResponse<UnreadCountResponse>> getUnreadCount(
            @Authenticated UUID userId) {

        log.info("[GET /notifications/unread-count] Getting unread count for user: {}", userId);

        long count = notificationService.getUnreadCount(userId);
        UnreadCountResponse response = new UnreadCountResponse(userId, count);
        return successSingle(response, "Unread count retrieved successfully");
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<SuccessResponse> markAsRead(
            @PathVariable UUID notificationId,
            @Authenticated UUID userId) {

        log.info("[PUT /notifications/{notificationId}/read] Marking notification {} as read for user: {}",
                notificationId, userId);

        notificationService.markAsRead(notificationId, userId);
        return success("Notification marked as read");
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<SuccessResponse> markAllAsRead(
            @Authenticated UUID userId) {

        log.info("[PUT /notifications/read-all] Marking all notifications as read for user: {}", userId);

        notificationService.markAllAsRead(userId);
        return success("All notifications marked as read");
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a notification")
    public ResponseEntity<SuccessResponse> deleteNotification(
            @PathVariable UUID notificationId,
            @Authenticated UUID userId) {

        log.info("[DELETE /notifications/{notificationId}] Deleting notification {} for user: {}",
                notificationId, userId);

        notificationService.deleteNotification(notificationId, userId);
        return success("Notification deleted successfully");
    }
}
