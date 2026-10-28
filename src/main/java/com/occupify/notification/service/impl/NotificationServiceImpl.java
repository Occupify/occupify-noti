package com.occupify.notification.service.impl;

import com.occupify.notification.dto.base.PageResponse;
import com.occupify.notification.dto.event.NotificationEventPayload;
import com.occupify.notification.dto.event.UserActivatedEvent;
import com.occupify.notification.dto.event.UserRegisteredEvent;
import com.occupify.notification.dto.response.NotificationResponse;
import com.occupify.notification.entity.Notification;
import com.occupify.notification.entity.User;
import com.occupify.notification.enums.NotificationCategory;
import com.occupify.notification.enums.NotificationTemplate;
import com.occupify.notification.exception.NotificationErrorCode;
import com.occupify.notification.exception.NotificationException;
import com.occupify.notification.mapper.NotificationMapper;
import com.occupify.notification.repository.NotificationRepository;
import com.occupify.notification.repository.UserRepository;
import com.occupify.notification.service.NotificationService;
import com.occupify.notification.service.NotificationStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationStreamService notificationStreamService;

    @Override
    @Transactional
    public void createProposalViewedNotification(UUID userId, String employerName, String jobTitle) {
        NotificationTemplate template = NotificationTemplate.PROPOSAL_VIEWED;
        processNotification(userId, template.getTitle(), template.formatContent(employerName, jobTitle), template);
    }

    @Override
    @Transactional
    public void createProposalSubmittedNotification(UUID userId, String freelancerName, String jobTitle) {
        NotificationTemplate template = NotificationTemplate.PROPOSAL_SUBMITTED;
        processNotification(userId, template.getTitle(), template.formatContent(freelancerName, jobTitle), template);
    }

    @Override
    @Transactional
    public void createProposalAcceptedNotification(UUID userId, String clientName, String jobTitle) {
        NotificationTemplate template = NotificationTemplate.PROPOSAL_ACCEPTED;
        processNotification(userId, template.getTitle(), template.formatContent(jobTitle, clientName), template);
    }

    @Override
    @Transactional
    public void createPostLikedNotification(UUID userId, String actorName, String postTitle) {
        NotificationTemplate template = NotificationTemplate.POST_LIKED;
        processNotification(userId, template.getTitle(), template.formatContent(actorName, postTitle), template);
    }

    @Override
    @Transactional
    public void createPostCommentedNotification(UUID userId, String actorName, String postTitle, String commentPreview) {
        NotificationTemplate template = NotificationTemplate.POST_COMMENTED;
        processNotification(userId, template.getTitle(), template.formatContent(actorName, postTitle, commentPreview), template);
    }

    @Override
    @Transactional
    public void createPostSharedNotification(UUID userId, String actorName, String postTitle) {
        NotificationTemplate template = NotificationTemplate.POST_SHARED;
        processNotification(userId, template.getTitle(), template.formatContent(actorName, postTitle), template);
    }

    @Override
    @Transactional
    public void createJobMatchPostedNotification(UUID userId, String jobTitle, String budget) {
        NotificationTemplate template = NotificationTemplate.JOB_MATCH_POSTED;
        processNotification(userId, template.getTitle(), template.formatContent(jobTitle, budget), template);
    }

    @Override
    @Transactional
    public void createContractInvitationNotification(UUID userId, String email, String employerName, String jobTitle, String message, String details) {
        NotificationTemplate template = NotificationTemplate.CONTRACT_INVITATION;
        String title = "Bạn có một lời mời tham gia hợp đồng từ " + (employerName != null ? employerName : "Nhà tuyển dụng");
        StringBuilder sb = new StringBuilder();
        if (jobTitle != null && !jobTitle.isBlank()) {
            sb.append("Công việc: ").append(jobTitle).append(". ");
        }
        if (message != null && !message.isBlank()) {
            sb.append("Tin nhắn: ").append(message).append(". ");
        }
        if (details != null && !details.isBlank()) {
            sb.append("Chi tiết: ").append(details);
        }
        String content = sb.toString().trim();
        processNotification(userId, title, content, template, email, null, null);
    }

    @Override
    @Transactional
    public void createContractAcceptedNotification(UUID employerId, String jobTitle) {
        createContractAcceptedNotification(employerId, null, null, jobTitle);
    }

    @Override
    @Transactional
    public void createContractAcceptedNotification(UUID employerId, String employerEmail, String freelancerName, String jobTitle) {
        NotificationTemplate template = NotificationTemplate.CONTRACT_ACCEPTED;
        String title = template.getTitle();
        String candidate = (freelancerName != null && !freelancerName.isBlank()) ? freelancerName : "Ứng viên";
        String job = (jobTitle != null && !jobTitle.isBlank()) ? jobTitle : "công việc";
        String content = String.format("%s đã chấp nhận hợp đồng cho công việc '%s'.", candidate, job);
        processNotification(employerId, title, content, template, employerEmail, null, null);
    }

    @Override
    @Transactional
    public void createContractRejectedNotification(UUID employerId, String reason) {
        createContractRejectedNotification(employerId, null, null, null, reason);
    }

    @Override
    @Transactional
    public void createContractRejectedNotification(UUID employerId, String employerEmail, String freelancerName, String jobTitle, String reason) {
        NotificationTemplate template = NotificationTemplate.CONTRACT_REJECTED;
        String title = template.getTitle();
        String candidate = (freelancerName != null && !freelancerName.isBlank()) ? freelancerName : "Ứng viên";
        String job = (jobTitle != null && !jobTitle.isBlank()) ? jobTitle : "công việc";
        String reasonText = (reason != null && !reason.isBlank()) ? reason : "Không có lý do";
        String content = String.format("%s đã từ chối hợp đồng cho công việc '%s'. Lý do: %s", candidate, job, reasonText);
        processNotification(employerId, title, content, template, employerEmail, null, null);
    }

    @Override
    @Transactional
    public void createContractCompensatedNotification(UUID employerId, String reason) {
        NotificationTemplate template = NotificationTemplate.CONTRACT_COMPENSATED;
        String title = template.getTitle();
        String content = template.formatContent("hợp đồng", reason != null ? reason : "Lỗi xử lý thanh toán");
        processNotification(employerId, title, content, template);
    }

    @Override
    @Transactional
    public NotificationResponse processNotificationEvent(NotificationEventPayload event) {
        NotificationTemplate template = event.actionType();
        String title = (event.customTitle() != null && !event.customTitle().isBlank())
                ? event.customTitle()
                : (template != null ? template.getTitle() : "Thông báo mới");

        String content;
        if (event.customContent() != null && !event.customContent().isBlank()) {
            content = event.customContent();
        } else if (template != null && event.args() != null && !event.args().isEmpty()) {
            content = template.formatContent(event.args().toArray());
        } else if (template != null) {
            content = template.getContentTemplate();
        } else {
            content = "Bạn có một thông báo mới.";
        }

        Notification notification = processNotification(event.recipientUserId(), title, content, template,
                event.recipientEmail(), event.recipientFullName(), event.recipientAvatarUrl());
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void processUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("[Notification Service] Processing user registered event for user [{}] ({})",
                event.userId(), event.email());
        getOrCreateUser(event.userId(), event.email(), null, null);
    }

    @Override
    @Transactional
    public void processUserActivatedEvent(UserActivatedEvent event) {
        log.info("[Notification Service] Processing user activated event for user [{}] ({})",
                event.userId(), event.email());
        getOrCreateUser(event.userId(), event.email(), event.fullName(), null);
    }

    private Notification processNotification(UUID userId, String title, String content, NotificationTemplate template) {
        return processNotification(userId, title, content, template, null, null, null);
    }

    private Notification processNotification(UUID userId, String title, String content, NotificationTemplate template,
                                             String email, String fullName, String avatarUrl) {
        if (template == null) {
            throw new NotificationException(NotificationErrorCode.INVALID_PAYLOAD);
        }
        User user = getOrCreateUser(userId, email, fullName, avatarUrl);
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(template.getCategory())
                .actionType(template)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("[Notification Service] Created notification [{}] for user: {}", saved.getId(), userId);

        NotificationResponse response = notificationMapper.toResponse(saved);
        if (notificationStreamService != null) {
            notificationStreamService.sendNotification(userId, response);
        }

        return saved;
    }

    private User getOrCreateUser(UUID userId, String email, String fullName, String avatarUrl) {
        return userRepository.findById(userId).orElseGet(() -> {
            if (email != null && !email.isBlank()) {
                java.util.Optional<User> existing = userRepository.findByEmail(email);
                if (existing.isPresent()) {
                    return existing.get();
                }
            }
            String resolvedEmail = (email != null && !email.isBlank()) ? email : "user_" + userId + "@occupify.local";
            User user = User.builder()
                    .id(userId)
                    .email(resolvedEmail)
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(user);
        });
    }

    @Override
    public PageResponse<NotificationResponse> getNotifications(UUID userId, int page, int limit) {
        Page<Notification> notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, limit));

        List<NotificationResponse> content = notificationMapper.toResponseList(notificationPage.getContent());

        return new PageResponse<>(
                200,
                "Notifications retrieved successfully",
                content,
                new PageResponse.PagingInfo(
                        page,
                        limit,
                        notificationPage.getTotalElements(),
                        notificationPage.getTotalPages()
                )
        );
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new NotificationException(NotificationErrorCode.FORBIDDEN);
        }

        notificationRepository.delete(notification);
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}
