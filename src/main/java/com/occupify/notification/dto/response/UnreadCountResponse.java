package com.occupify.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {
    private UUID userId;
    private long unreadCount;
}
