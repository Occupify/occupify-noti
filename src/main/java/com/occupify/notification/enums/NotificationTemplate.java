package com.occupify.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {
    PROPOSAL_VIEWED("Đề xuất đã được xem", "Nhà tuyển dụng %s đã xem đề xuất của bạn cho công việc '%s'.", NotificationCategory.JOB),
    PROPOSAL_SUBMITTED("Đề xuất ứng tuyển mới", "Ứng viên %s vừa gửi đề xuất cho công việc '%s'.", NotificationCategory.JOB),
    PROPOSAL_ACCEPTED("Đề xuất đã được chấp nhận", "Chúc mừng! Đề xuất của bạn cho công việc '%s' đã được %s chấp nhận.", NotificationCategory.CONTRACT),
    POST_LIKED("Tương tác mới", "%s đã thích bài viết '%s' của bạn.", NotificationCategory.JOB),
    POST_COMMENTED("Bình luận mới", "%s đã bình luận về bài viết '%s' của bạn: \"%s\"", NotificationCategory.JOB),
    POST_SHARED("Chia sẻ mới", "%s đã chia sẻ bài viết '%s' của bạn.", NotificationCategory.JOB),
    JOB_MATCH_POSTED("Công việc phù hợp mới", "Có công việc mới phù hợp với kỹ năng của bạn: '%s' (Ngân sách: %s).", NotificationCategory.JOB);

    private final String title;
    private final String contentTemplate;
    private final NotificationCategory category;

    public String formatContent(Object... args) {
        return String.format(this.contentTemplate, args);
    }
}
