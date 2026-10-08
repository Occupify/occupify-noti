package com.occupify.notification.service.impl;

import com.occupify.notification.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@occupify.com}")
    private String fromEmail;

    @Value("${app.otp.expiration-minutes:5}")
    private long expirationMinutes;

    @Override
    @Async
    public void sendRegistrationOtp(String recipientEmail, String otpCode) {
        sendHtmlEmail(
                recipientEmail,
                "Occupify - Mã kích hoạt tài khoản",
                "templates/email/registration-otp.html",
                otpCode,
                "registration OTP"
        );
    }

    @Override
    @Async
    public void sendPasswordResetOtp(String recipientEmail, String otpCode) {
        sendHtmlEmail(
                recipientEmail,
                "Occupify - Mã đặt lại mật khẩu",
                "templates/email/password-reset.html",
                otpCode,
                "password reset OTP"
        );
    }

    private void sendHtmlEmail(String recipientEmail, String subject, String templatePath, String otpCode, String actionDescription) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[Email Service] Cannot send email: recipient email is null or empty");
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
            String from = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : "no-reply@occupify.com";
            helper.setFrom(from);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);

            String htmlContent = loadTemplateContent(templatePath, otpCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[Email Service] Successfully dispatched {} email to [{}]", actionDescription, recipientEmail);
        } catch (Exception ex) {
            log.error("[Email Service] Failed to dispatch {} email to [{}]: {}", actionDescription, recipientEmail, ex.getMessage());
        }
    }

    private String loadTemplateContent(String templatePath, String otpCode) {
        try {
            return new ClassPathResource(templatePath)
                    .getContentAsString(StandardCharsets.UTF_8)
                    .replace("{{otp}}", otpCode)
                    .replace("{{expiryMinutes}}", String.valueOf(expirationMinutes));
        } catch (Exception e) {
            log.warn("[Email Service] Failed to load email template [{}], using fallback: {}", templatePath, e.getMessage());
            return "Your Occupify code is: " + otpCode + " (expires in " + expirationMinutes + " minutes)";
        }
    }
}
