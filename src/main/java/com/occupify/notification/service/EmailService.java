package com.occupify.notification.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EmailService {

    void sendRegistrationOtp(String recipientEmail, String otpCode);

    void sendPasswordResetOtp(String recipientEmail, String otpCode);

    void sendContractInvitationEmail(
            String recipientEmail,
            String employerName,
            String jobTitle,
            String message,
            String role,
            BigDecimal agreedRate,
            String salaryType,
            LocalDateTime startedAt,
            LocalDateTime finishedAt);

    void sendContractAcceptedEmail(
            String recipientEmail,
            String freelancerName,
            String jobTitle,
            UUID contractId);

    void sendContractRejectedEmail(
            String recipientEmail,
            String freelancerName,
            String jobTitle,
            String reason);
}
