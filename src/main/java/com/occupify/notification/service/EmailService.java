package com.occupify.notification.service;

public interface EmailService {

    void sendRegistrationOtp(String recipientEmail, String otpCode);

    void sendPasswordResetOtp(String recipientEmail, String otpCode);
}
