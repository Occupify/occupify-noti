package com.occupify.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.occupify.notification.dto.base.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    @JsonProperty("unread_notification")
    private long unreadCount;
    private PageResponse.PagingInfo paging;
}
