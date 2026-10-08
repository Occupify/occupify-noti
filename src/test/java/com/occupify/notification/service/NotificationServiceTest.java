package com.occupify.notification.service;

import com.occupify.notification.dto.base.PageResponse;
import com.occupify.notification.dto.event.NotificationEventPayload;
import com.occupify.notification.dto.response.NotificationResponse;
import com.occupify.notification.entity.Notification;
import com.occupify.notification.entity.User;
import com.occupify.notification.enums.NotificationCategory;
import com.occupify.notification.enums.NotificationTemplate;
import com.occupify.notification.mapper.NotificationMapper;
import com.occupify.notification.repository.NotificationRepository;
import com.occupify.notification.repository.UserRepository;
import com.occupify.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationStreamService streamService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .email("user@occupify.com")
                .fullName("User Test")
                .build();
    }

    @Test
    @DisplayName("Should create proposal viewed notification")
    void shouldCreateProposalViewedNotification() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        notificationService.createProposalViewedNotification(userId, "TechCorp", "Java Developer");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should process notification event and return response")
    void shouldProcessNotificationEvent() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        NotificationResponse responseDto = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Đề xuất đã được xem")
                .content("Nhà tuyển dụng TechCorp đã xem đề xuất của bạn cho công việc 'Java'.")
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.PROPOSAL_VIEWED)
                .isRead(false)
                .build();

        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(responseDto);

        NotificationEventPayload payload = NotificationEventPayload.builder()
                .eventId(UUID.randomUUID())
                .recipientUserId(userId)
                .actionType(NotificationTemplate.PROPOSAL_VIEWED)
                .args(List.of("TechCorp", "Java"))
                .build();

        NotificationResponse response = notificationService.processNotificationEvent(payload);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should get paginated notifications")
    void shouldGetNotifications() {
        Notification notification = Notification.builder()
                .user(testUser)
                .title("Test")
                .content("Test Content")
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.POST_LIKED)
                .isRead(false)
                .build();
        notification.setId(UUID.randomUUID());

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        NotificationResponse responseDto = NotificationResponse.builder()
                .id(notification.getId())
                .userId(userId)
                .title("Test")
                .content("Test Content")
                .build();

        when(notificationMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));

        PageResponse<NotificationResponse> result = notificationService.getNotifications(userId, 0, 10);

        assertNotNull(result);
        assertEquals(200, result.statusCode());
        assertEquals(1, result.data().size());
        assertEquals(1, result.paging().total());
    }

    @Test
    @DisplayName("Should mark notification as read")
    void shouldMarkAsRead() {
        UUID notiId = UUID.randomUUID();
        notificationService.markAsRead(notiId, userId);

        verify(notificationRepository).markAsRead(eq(notiId), eq(userId), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should process user registered event and sync user")
    void shouldProcessUserRegisteredEvent() {
        com.occupify.notification.dto.event.UserRegisteredEvent event =
                new com.occupify.notification.dto.event.UserRegisteredEvent(userId, "newuser@occupify.com", "123456", java.time.Instant.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        notificationService.processUserRegisteredEvent(event);

        verify(userRepository).findById(userId);
    }
}
