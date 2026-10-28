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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Override
    @Async
    public void sendContractInvitationEmail(
            String recipientEmail,
            String employerName,
            String jobTitle,
            String message,
            String role,
            BigDecimal agreedRate,
            String salaryType,
            LocalDateTime startedAt,
            LocalDateTime finishedAt) {
        String subject = "Occupify - Lời mời tham gia hợp đồng từ " + (employerName != null ? employerName : "Nhà tuyển dụng");
        StringBuilder body = new StringBuilder();
        body.append("<h2>Bạn có một lời mời tham gia hợp đồng từ ").append(employerName != null ? employerName : "Nhà tuyển dụng").append("</h2>");
        if (jobTitle != null && !jobTitle.isBlank()) {
            body.append("<p><strong>Công việc:</strong> ").append(jobTitle).append("</p>");
        }
        if (message != null && !message.isBlank()) {
            body.append("<p><strong>Tin nhắn:</strong> ").append(message).append("</p>");
        }
        body.append("<div style='margin-top:16px;padding:12px;background:#f5f5f5;border-radius:4px;'>");
        body.append("<h4>Chi tiết hợp đồng:</h4>");
        if (role != null) body.append("<p>Vị trí: ").append(role).append("</p>");
        if (agreedRate != null) body.append("<p>Mức thù lao: ").append(agreedRate).append(" ").append(salaryType != null ? salaryType : "").append("</p>");
        if (startedAt != null) body.append("<p>Ngày bắt đầu: ").append(startedAt).append("</p>");
        if (finishedAt != null) body.append("<p>Ngày kết thúc dự kiến: ").append(finishedAt).append("</p>");
        body.append("</div>");
        sendSimpleHtmlEmail(recipientEmail, subject, body.toString(), "contract invitation");
    }

    @Override
    @Async
    public void sendContractAcceptedEmail(
            String recipientEmail,
            String freelancerName,
            String jobTitle,
            UUID contractId) {
        String subject = "Occupify - Hợp đồng đã được chấp nhận: " + (jobTitle != null ? jobTitle : "Hợp đồng mới");
        String body = String.format("<h2>Hợp đồng đã được chấp nhận!</h2>" +
                "<p>Ứng viên <strong>%s</strong> đã chấp nhận lời mời tham gia hợp đồng cho công việc <strong>'%s'</strong>.</p>" +
                "<p>Mã hợp đồng: %s</p>",
                freelancerName != null ? freelancerName : "Ứng viên",
                jobTitle != null ? jobTitle : "công việc",
                contractId != null ? contractId : "N/A");
        sendSimpleHtmlEmail(recipientEmail, subject, body, "contract accepted");
    }

    @Override
    @Async
    public void sendContractRejectedEmail(
            String recipientEmail,
            String freelancerName,
            String jobTitle,
            String reason) {
        String subject = "Occupify - Lời mời hợp đồng bị từ chối: " + (jobTitle != null ? jobTitle : "Hợp đồng");
        String body = String.format("<h2>Lời mời hợp đồng bị từ chối</h2>" +
                "<p>Ứng viên <strong>%s</strong> đã từ chối hợp đồng cho công việc <strong>'%s'</strong>.</p>" +
                "<p><strong>Lý do:</strong> %s</p>",
                freelancerName != null ? freelancerName : "Ứng viên",
                jobTitle != null ? jobTitle : "công việc",
                reason != null && !reason.isBlank() ? reason : "Không có lý do cụ thể");
        sendSimpleHtmlEmail(recipientEmail, subject, body, "contract rejected");
    }

    private void sendSimpleHtmlEmail(String recipientEmail, String subject, String htmlContent, String actionDescription) {
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
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[Email Service] Successfully dispatched {} email to [{}]", actionDescription, recipientEmail);
        } catch (Exception ex) {
            log.warn("[Email Service] Could not send {} email to [{}] via SMTP ({}), notification persisted to DB",
                    actionDescription, recipientEmail, ex.getMessage());
        }
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
