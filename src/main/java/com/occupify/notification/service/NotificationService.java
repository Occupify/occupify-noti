package com.occupify.notification.service;

import com.occupify.notification.dto.base.PageResponse;
import com.occupify.notification.dto.event.NotificationEventPayload;
import com.occupify.notification.dto.event.UserRegisteredEvent;
import com.occupify.notification.dto.response.NotificationResponse;

import java.util.UUID;

public interface NotificationService {

    void createProposalViewedNotification(UUID userId, String employerName, String jobTitle);

    void createProposalSubmittedNotification(UUID userId, String freelancerName, String jobTitle);

    void createProposalAcceptedNotification(UUID userId, String clientName, String jobTitle);

    void createPostLikedNotification(UUID userId, String actorName, String postTitle);

    void createPostCommentedNotification(UUID userId, String actorName, String postTitle, String commentPreview);

    void createPostSharedNotification(UUID userId, String actorName, String postTitle);

    void createJobMatchPostedNotification(UUID userId, String jobTitle, String budget);

    NotificationResponse processNotificationEvent(NotificationEventPayload event);

    void processUserRegisteredEvent(UserRegisteredEvent event);

    PageResponse<NotificationResponse> getNotifications(UUID userId, int page, int limit);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);

    long getUnreadCount(UUID userId);
}
