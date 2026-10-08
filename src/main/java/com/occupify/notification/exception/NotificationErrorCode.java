package com.occupify.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode {
    NOTIFICATION_NOT_FOUND("NOTI_001", "Notification not found", HttpStatus.NOT_FOUND),
    FORBIDDEN("NOTI_002", "Access forbidden", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("NOTI_003", "User not found", HttpStatus.NOT_FOUND),
    INVALID_PAYLOAD("NOTI_004", "Invalid notification payload", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
