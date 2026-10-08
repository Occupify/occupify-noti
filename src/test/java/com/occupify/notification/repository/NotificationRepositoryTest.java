package com.occupify.notification.repository;

import com.occupify.notification.entity.Notification;
import com.occupify.notification.entity.User;
import com.occupify.notification.enums.NotificationCategory;
import com.occupify.notification.enums.NotificationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("testuser@occupify.com")
                .fullName("Test User")
                .avatarUrl("https://example.com/avatar.jpg")
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("Should persist and find notification by user ID")
    void shouldPersistAndFindByUserId() {
        Notification notification = Notification.builder()
                .user(testUser)
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.PROPOSAL_VIEWED)
                .title("Proposal Viewed")
                .content("TechCorp has viewed your proposal.")
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        assertNotNull(saved.getId());

        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                testUser.getId(), PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals(saved.getId(), page.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Should count unread notifications correctly")
    void shouldCountUnreadNotifications() {
        notificationRepository.save(Notification.builder()
                .user(testUser)
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.PROPOSAL_SUBMITTED)
                .title("Proposal Submitted")
                .content("Content 1")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(testUser)
                .category(NotificationCategory.CONTRACT)
                .actionType(NotificationTemplate.PROPOSAL_ACCEPTED)
                .title("Proposal Accepted")
                .content("Content 2")
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build());

        long unreadCount = notificationRepository.countUnreadByUserId(testUser.getId());
        assertEquals(1, unreadCount);
    }

    @Test
    @DisplayName("Should mark all notifications as read for a user")
    void shouldMarkAllAsRead() {
        notificationRepository.save(Notification.builder()
                .user(testUser)
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.POST_LIKED)
                .title("Post Liked")
                .content("User liked your post")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(testUser)
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.POST_COMMENTED)
                .title("Post Commented")
                .content("User commented on your post")
                .isRead(false)
                .build());

        notificationRepository.markAllAsRead(testUser.getId(), LocalDateTime.now());

        long unreadAfter = notificationRepository.countUnreadByUserId(testUser.getId());
        assertEquals(0, unreadAfter);
    }
}
