package com.occupify.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.occupify.notification.config.AuthenticatedArgumentResolver;
import com.occupify.notification.config.WebMvcConfig;
import com.occupify.notification.dto.base.PageResponse;
import com.occupify.notification.dto.response.NotificationResponse;
import com.occupify.notification.enums.NotificationCategory;
import com.occupify.notification.enums.NotificationTemplate;
import com.occupify.notification.config.ScalarConfig;
import com.occupify.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import({ResponseFactory.class, WebMvcConfig.class, AuthenticatedArgumentResolver.class, ScalarConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET /api/v1/notifications should return paginated notifications")
    void shouldReturnNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationResponse item = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .category(NotificationCategory.JOB)
                .actionType(NotificationTemplate.PROPOSAL_VIEWED)
                .title("Proposal Viewed")
                .content("TechCorp has viewed your proposal.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        PageResponse<NotificationResponse> pageResponse = new PageResponse<>(
                200, "Notifications retrieved successfully", List.of(item),
                new PageResponse.PagingInfo(0, 10, 1, 1));

        when(notificationService.getNotifications(userId, 0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/notifications")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data[0].title").value("Proposal Viewed"));
    }

    @Test
    @DisplayName("GET /notifications should return 403 when no authentication provided")
    void shouldReturnForbiddenWhenNoAuth() throws Exception {
        mockMvc.perform(get("/notifications")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /notifications/unread-count should return unread count")
    void shouldReturnUnreadCount() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationService.getUnreadCount(userId)).thenReturn(5L);

        mockMvc.perform(get("/notifications/unread-count")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(5));
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read should mark as read")
    void shouldMarkAsRead() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notiId = UUID.randomUUID();

        mockMvc.perform(put("/notifications/" + notiId + "/read")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Notification marked as read"));

        verify(notificationService).markAsRead(notiId, userId);
    }

    @Test
    @DisplayName("PUT /notifications/read-all should mark all as read")
    void shouldMarkAllAsRead() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(put("/notifications/read-all")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));

        verify(notificationService).markAllAsRead(userId);
    }

    @Test
    @DisplayName("DELETE /notifications/{id} should delete notification")
    void shouldDeleteNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notiId = UUID.randomUUID();

        mockMvc.perform(delete("/notifications/" + notiId)
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));

        verify(notificationService).deleteNotification(notiId, userId);
    }

    @Test
    @DisplayName("GET /docs should forward to /scalar.html")
    void shouldForwardDocsToScalarHtml() throws Exception {
        mockMvc.perform(get("/docs"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/scalar.html"));
    }

    @Test
    @DisplayName("GET /scalar should forward to /scalar.html")
    void shouldForwardScalarToScalarHtml() throws Exception {
        mockMvc.perform(get("/scalar"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/scalar.html"));
    }
}
