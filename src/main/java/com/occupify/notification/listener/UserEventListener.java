package com.occupify.notification.listener;

import com.occupify.notification.dto.event.PasswordResetRequestedEvent;
import com.occupify.notification.dto.event.UserActivatedEvent;
import com.occupify.notification.dto.event.UserRegisteredEvent;
import com.occupify.notification.service.EmailService;
import com.occupify.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${app.rabbitmq.user-queue:occupify.notification.user.queue}")
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitHandler
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("[Event Listener] Processing user registered event (send OTP) for user [{}] ({})",
                event.userId(), event.email());
        log.info(">> [NOTIFICATION DEV OTP] Registration OTP for [{}]: {}", event.email(), event.otpCode());
        if (event.email() != null && !event.email().isBlank() && event.otpCode() != null && !event.otpCode().isBlank()) {
            emailService.sendRegistrationOtp(event.email(), event.otpCode());
        }
    }

    @RabbitHandler
    public void handleUserActivatedEvent(UserActivatedEvent event) {
        log.info("[Event Listener] Processing user activated event for user [{}] ({})",
                event.userId(), event.email());
        notificationService.processUserActivatedEvent(event);
    }

    @RabbitHandler
    public void handlePasswordResetRequestedEvent(PasswordResetRequestedEvent event) {
        log.info("[Event Listener] Processing password reset event for user [{}] ({})",
                event.userId(), event.email());
        if (event.email() != null && !event.email().isBlank() && event.otpCode() != null && !event.otpCode().isBlank()) {
            emailService.sendPasswordResetOtp(event.email(), event.otpCode());
        }
    }
}

