package com.occupify.notification.service;

import com.occupify.notification.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@occupify.com");
        ReflectionTestUtils.setField(emailService, "expirationMinutes", 5L);
    }

    @Test
    @DisplayName("Should send registration OTP email successfully")
    void shouldSendRegistrationOtpSuccessfully() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendRegistrationOtp("user@occupify.com", "123456");

        verify(mailSender).send(mimeMessage);
        assertEquals("Occupify - Mã kích hoạt tài khoản", mimeMessage.getSubject());
        assertEquals("user@occupify.com", mimeMessage.getAllRecipients()[0].toString());
    }

    @Test
    @DisplayName("Should send password reset OTP email successfully")
    void shouldSendPasswordResetOtpSuccessfully() throws Exception {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetOtp("user@occupify.com", "654321");

        verify(mailSender).send(mimeMessage);
        assertEquals("Occupify - Mã đặt lại mật khẩu", mimeMessage.getSubject());
        assertEquals("user@occupify.com", mimeMessage.getAllRecipients()[0].toString());
    }

    @Test
    @DisplayName("Should not send email when recipient is null or blank")
    void shouldNotSendEmailWhenRecipientBlank() {
        emailService.sendRegistrationOtp(null, "123456");
        emailService.sendRegistrationOtp("  ", "123456");
        emailService.sendPasswordResetOtp("", "654321");

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should catch and handle MailException gracefully without throwing")
    void shouldHandleMailExceptionGracefully() {
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server connection failed")).when(mailSender).send(mimeMessage);

        assertDoesNotThrow(() -> emailService.sendRegistrationOtp("user@occupify.com", "123456"));
    }
}
