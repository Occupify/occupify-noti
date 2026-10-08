package com.occupify.notification.listener;

import com.occupify.notification.dto.event.PasswordResetRequestedEvent;
import com.occupify.notification.dto.event.UserRegisteredEvent;
import com.occupify.notification.service.EmailService;
import com.occupify.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserEventListener listener;

    @Test
    @DisplayName("Should handle user registered event and dispatch registration OTP email")
    void shouldHandleUserRegisteredEvent() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(userId, "newuser@occupify.com", "123456", Instant.now());

        listener.handleUserRegisteredEvent(event);

        verify(notificationService).processUserRegisteredEvent(event);
        verify(emailService).sendRegistrationOtp("newuser@occupify.com", "123456");
    }

    @Test
    @DisplayName("Should handle user registered event without dispatching email when email or otp is blank")
    void shouldHandleUserRegisteredEvent_whenBlankEmailOrOtp() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(userId, "", null, Instant.now());

        listener.handleUserRegisteredEvent(event);

        verify(notificationService).processUserRegisteredEvent(event);
        verify(emailService, never()).sendRegistrationOtp(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Should handle password reset requested event and dispatch reset OTP email")
    void shouldHandlePasswordResetRequestedEvent() {
        UUID userId = UUID.randomUUID();
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(userId, "reset@occupify.com", "654321", Instant.now());

        listener.handlePasswordResetRequestedEvent(event);

        verify(emailService).sendPasswordResetOtp("reset@occupify.com", "654321");
    }

    @Test
    @DisplayName("Should not dispatch password reset OTP email when email or otp is blank")
    void shouldHandlePasswordResetRequestedEvent_whenBlankEmailOrOtp() {
        UUID userId = UUID.randomUUID();
        PasswordResetRequestedEvent event = new PasswordResetRequestedEvent(userId, null, " ", Instant.now());

        listener.handlePasswordResetRequestedEvent(event);

        verify(emailService, never()).sendPasswordResetOtp(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}

