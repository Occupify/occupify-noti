package com.occupify.notification.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.occupify.notification.dto.event.ContractAcceptedEvent;
import com.occupify.notification.dto.event.ContractCancelledEvent;
import com.occupify.notification.dto.event.ContractCompensationRequestedEvent;
import com.occupify.notification.dto.event.ContractCompletedEvent;
import com.occupify.notification.dto.event.ContractCreatedEvent;
import com.occupify.notification.dto.event.ContractRejectedEvent;
import com.occupify.notification.dto.event.ReviewCreatedEvent;
import com.occupify.notification.service.EmailService;
import com.occupify.notification.service.NotificationService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ContractEventListener contractEventListener;

    private final UUID employerId = UUID.randomUUID();
    private final UUID freelancerId = UUID.randomUUID();
    private final UUID contractId = UUID.randomUUID();

    @Test
    @DisplayName("Should send email and create notification on ContractCreatedEvent")
    void handleContractCreated_SendsEmailAndCreatesNotification() {
        ContractCreatedEvent event = new ContractCreatedEvent(
                UUID.randomUUID(), contractId, UUID.randomUUID(), UUID.randomUUID(),
                employerId, freelancerId, "freelancer@test.com", "Welcome to the team",
                "Developer", "Backend Engineer", new BigDecimal("1000.00"), "FIXED",
                LocalDateTime.now(), LocalDateTime.now().plusMonths(2), LocalDateTime.now(),
                "Tech Corp", "employer@test.com");

        contractEventListener.handleContractCreated(event);

        verify(emailService).sendContractInvitationEmail(
                eq("freelancer@test.com"), eq("Tech Corp"), eq("Backend Engineer"),
                eq("Welcome to the team"), eq("Developer"), eq(new BigDecimal("1000.00")),
                eq("FIXED"), any(), any());

        verify(notificationService).createContractInvitationNotification(
                eq(freelancerId), eq("freelancer@test.com"), eq("Tech Corp"),
                eq("Backend Engineer"), eq("Welcome to the team"), any());
    }

    @Test
    @DisplayName("Should create contract accepted notification and email on ContractAcceptedEvent")
    void handleContractAccepted_CreatesNotification() {
        ContractAcceptedEvent event = new ContractAcceptedEvent(
                UUID.randomUUID(), contractId, UUID.randomUUID(), UUID.randomUUID(),
                employerId, freelancerId, "Backend Engineer Contract", new BigDecimal("500.00"),
                "FIXED", LocalDate.now(), LocalDate.now().plusMonths(1), LocalDateTime.now(),
                "employer@test.com", "Employer Corp", "John Dev", "freelancer@test.com");

        contractEventListener.handleContractAccepted(event);

        verify(emailService).sendContractAcceptedEmail("employer@test.com", "John Dev", "Backend Engineer Contract", contractId);
        verify(notificationService).createContractAcceptedNotification(
                employerId, "employer@test.com", "John Dev", "Backend Engineer Contract");
    }

    @Test
    @DisplayName("Should create contract rejected notification and email on ContractRejectedEvent")
    void handleContractRejected_CreatesNotification() {
        ContractRejectedEvent event = new ContractRejectedEvent(
                UUID.randomUUID(), contractId, UUID.randomUUID(),
                employerId, freelancerId, "Schedule conflict", LocalDateTime.now(),
                "employer@test.com", "Employer Corp", "John Dev", "Backend Engineer Contract");

        contractEventListener.handleContractRejected(event);

        verify(emailService).sendContractRejectedEmail("employer@test.com", "John Dev", "Backend Engineer Contract", "Schedule conflict");
        verify(notificationService).createContractRejectedNotification(
                employerId, "employer@test.com", "John Dev", "Backend Engineer Contract", "Schedule conflict");
    }

    @Test
    @DisplayName("Should create contract compensation notification on ContractCompensationRequestedEvent")
    void handleContractCompensation_CreatesNotification() {
        ContractCompensationRequestedEvent event = new ContractCompensationRequestedEvent(
                UUID.randomUUID(), contractId, "occupify-payment", "INSUFFICIENT_FUNDS",
                "Payment failed", LocalDateTime.now());

        contractEventListener.handleContractCompensation(event);

        verify(notificationService).createContractCompensatedNotification(contractId, "Payment failed");
    }

    @Test
    @DisplayName("Should still persist in-app notification when emailService throws in handleContractCreated")
    void handleContractCreated_EmailFails_StillPersistsNotification() {
        doThrow(new RuntimeException("SMTP connection timeout")).when(emailService).sendContractInvitationEmail(
                any(), any(), any(), any(), any(), any(), any(), any(), any());

        ContractCreatedEvent event = new ContractCreatedEvent(
                UUID.randomUUID(), contractId, UUID.randomUUID(), UUID.randomUUID(),
                employerId, freelancerId, "freelancer@test.com", "Welcome",
                "Developer", "Backend Engineer", new BigDecimal("1000.00"), "FIXED",
                LocalDateTime.now(), LocalDateTime.now().plusMonths(2), LocalDateTime.now(),
                "Tech Corp", "employer@test.com");

        contractEventListener.handleContractCreated(event);

        verify(notificationService).createContractInvitationNotification(
                eq(freelancerId), eq("freelancer@test.com"), eq("Tech Corp"),
                eq("Backend Engineer"), eq("Welcome"), any());
    }

    @Test
    @DisplayName("Should still persist in-app notification when emailService throws in handleContractCompleted")
    void handleContractCompleted_EmailFails_StillPersistsNotification() {
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendNotificationEmail(
                any(), any(), any(), any());

        ContractCompletedEvent event = new ContractCompletedEvent(
                UUID.randomUUID(), contractId, UUID.randomUUID(), UUID.randomUUID(),
                employerId, freelancerId, employerId, "EMPLOYER",
                "Backend Engineer", new BigDecimal("1000.00"), "FIXED",
                LocalDateTime.now(), LocalDateTime.now(),
                "employer@test.com", "Tech Corp", "freelancer@test.com", "Freelancer");

        contractEventListener.handleContractCompleted(event);

        verify(notificationService).createContractCompletedNotification(eq(freelancerId), eq("freelancer@test.com"), eq("Backend Engineer"));
        verify(notificationService).createContractCompletedNotification(eq(employerId), eq("employer@test.com"), eq("Backend Engineer"));
    }

    @Test
    @DisplayName("Should still persist in-app notification when emailService throws in handleReviewCreated")
    void handleReviewCreated_EmailFails_StillPersistsNotification() {
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendNotificationEmail(
                any(), any(), any(), any());

        ReviewCreatedEvent event = new ReviewCreatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), contractId, employerId, "Tech Corp",
                freelancerId, "Freelancer", "freelancer@test.com", 5, "Great work", LocalDateTime.now());

        contractEventListener.handleReviewCreated(event);

        verify(notificationService).createReviewReceivedNotification(
                eq(freelancerId), eq("freelancer@test.com"), eq("Tech Corp"), eq(5), eq("Great work"));
    }
}
