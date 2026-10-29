package com.occupify.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum NotificationTemplate {
    PROPOSAL_VIEWED("Đề xuất đã được xem", "Nhà tuyển dụng %s đã xem đề xuất của bạn cho công việc '%s'.", NotificationCategory.JOB),
    PROPOSAL_SUBMITTED("Đề xuất ứng tuyển mới", "Ứng viên %s vừa gửi đề xuất cho công việc '%s'.", NotificationCategory.JOB),
    PROPOSAL_ACCEPTED("Đề xuất đã được chấp nhận", "Chúc mừng! Đề xuất của bạn cho công việc '%s' đã được %s chấp nhận.", NotificationCategory.CONTRACT),
    POST_LIKED("Tương tác mới", "%s đã thích bài viết '%s' của bạn.", NotificationCategory.JOB),
    POST_COMMENTED("Bình luận mới", "%s đã bình luận về bài viết '%s' của bạn: \"%s\"", NotificationCategory.JOB),
    POST_SHARED("Chia sẻ mới", "%s đã chia sẻ bài viết '%s' của bạn.", NotificationCategory.JOB),
    JOB_MATCH_POSTED("Công việc phù hợp mới", "Có công việc mới phù hợp với kỹ năng của bạn: '%s' (Ngân sách: %s).", NotificationCategory.JOB),
    CONTRACT_INVITATION("Lời mời tham gia hợp đồng", "Bạn có một lời mời tham gia hợp đồng từ %s cho công việc '%s'.", NotificationCategory.CONTRACT),
    CONTRACT_ACCEPTED("Hợp đồng đã được chấp nhận", "Ứng viên đã chấp nhận hợp đồng cho công việc '%s'.", NotificationCategory.CONTRACT),
    CONTRACT_REJECTED("Hợp đồng bị từ chối", "Ứng viên đã từ chối hợp đồng cho công việc '%s'. Lý do: %s", NotificationCategory.CONTRACT),
    CONTRACT_COMPENSATED("Kích hoạt hợp đồng thất bại", "Hợp đồng cho công việc '%s' không thể kích hoạt do sự cố hệ thống. Lý do: %s", NotificationCategory.CONTRACT),
    PROJECT_CREATED("Dự án đã được tạo", "Dự án '%s' của bạn đã được đăng thành công.", NotificationCategory.PROJECT),
    PROJECT_CLOSED("Dự án đã đóng", "Dự án '%s' của bạn đã được đóng.", NotificationCategory.PROJECT),
    PROPOSAL_CANCELLED("Đề xuất đã bị hủy", "Một đề xuất ứng tuyển cho dự án của bạn đã bị hủy.", NotificationCategory.JOB);

    private final String title;
    private final String contentTemplate;
    private final NotificationCategory category;

    NotificationTemplate(String title, String contentTemplate, NotificationCategory category) {
        this.title = title;
        this.contentTemplate = contentTemplate;
        this.category = category;
    }

    public String formatContent(Object... args) {
        return String.format(this.contentTemplate, args);
    }
}
