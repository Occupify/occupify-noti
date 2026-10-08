package com.occupify.notification.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateTest {

    @Test
    @DisplayName("Should format PROPOSAL_VIEWED template")
    void shouldFormatProposalViewed() {
        NotificationTemplate template = NotificationTemplate.PROPOSAL_VIEWED;
        assertEquals("Đề xuất đã được xem", template.getTitle());
        assertEquals(NotificationCategory.JOB, template.getCategory());

        String content = template.formatContent("TechCorp", "Senior Java Engineer");
        assertEquals("Nhà tuyển dụng TechCorp đã xem đề xuất của bạn cho công việc 'Senior Java Engineer'.", content);
    }

    @Test
    @DisplayName("Should format PROPOSAL_SUBMITTED template")
    void shouldFormatProposalSubmitted() {
        NotificationTemplate template = NotificationTemplate.PROPOSAL_SUBMITTED;
        assertEquals("Đề xuất ứng tuyển mới", template.getTitle());
        assertEquals(NotificationCategory.JOB, template.getCategory());

        String content = template.formatContent("Nguyen Van A", "Fullstack Developer");
        assertEquals("Ứng viên Nguyen Van A vừa gửi đề xuất cho công việc 'Fullstack Developer'.", content);
    }

    @Test
    @DisplayName("Should format PROPOSAL_ACCEPTED template")
    void shouldFormatProposalAccepted() {
        NotificationTemplate template = NotificationTemplate.PROPOSAL_ACCEPTED;
        assertEquals("Đề xuất đã được chấp nhận", template.getTitle());
        assertEquals(NotificationCategory.CONTRACT, template.getCategory());

        String content = template.formatContent("Backend System", "Global Corp");
        assertEquals("Chúc mừng! Đề xuất của bạn cho công việc 'Backend System' đã được Global Corp chấp nhận.", content);
    }

    @Test
    @DisplayName("Should format POST interactions templates")
    void shouldFormatPostInteractions() {
        String liked = NotificationTemplate.POST_LIKED.formatContent("Tran B", "Microservice Guide");
        assertEquals("Tran B đã thích bài viết 'Microservice Guide' của bạn.", liked);

        String commented = NotificationTemplate.POST_COMMENTED.formatContent("Tran B", "Microservice Guide", "Great read!");
        assertEquals("Tran B đã bình luận về bài viết 'Microservice Guide' của bạn: \"Great read!\"", commented);

        String shared = NotificationTemplate.POST_SHARED.formatContent("Tran B", "Microservice Guide");
        assertEquals("Tran B đã chia sẻ bài viết 'Microservice Guide' của bạn.", shared);
    }

    @Test
    @DisplayName("Should format JOB_MATCH_POSTED template")
    void shouldFormatJobMatchPosted() {
        String match = NotificationTemplate.JOB_MATCH_POSTED.formatContent("DevOps Engineer", "$3000");
        assertEquals("Có công việc mới phù hợp với kỹ năng của bạn: 'DevOps Engineer' (Ngân sách: $3000).", match);
    }
}
