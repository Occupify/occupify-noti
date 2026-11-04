package com.occupify.notification.listener;

import com.occupify.notification.dto.event.ContractAcceptedEvent;
import com.occupify.notification.dto.event.ContractCancelledEvent;
import com.occupify.notification.dto.event.ContractCompensationRequestedEvent;
import com.occupify.notification.dto.event.ContractCompletedEvent;
import com.occupify.notification.dto.event.ContractCreatedEvent;
import com.occupify.notification.dto.event.ContractRejectedEvent;
import com.occupify.notification.dto.event.ReviewCreatedEvent;
import com.occupify.notification.service.EmailService;
import com.occupify.notification.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@RabbitListener(queues = "${app.rabbitmq.contract-queue:occupify.notification.contract.queue}")
public class ContractEventListener {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitHandler
    public void handleContractCreated(ContractCreatedEvent event) {
        log.info("[Notification Contract Listener] Processing ContractCreatedEvent for contractId: {}, freelancerEmail: {}",
                event.contractId(), event.freelancerEmail());

        String employerName = (event.employerName() != null && !event.employerName().isBlank())
                ? event.employerName()
                : "Nhà tuyển dụng";
        String jobTitle = (event.title() != null && !event.title().isBlank())
                ? event.title()
                : "Công việc";

        // 1. Send invitation email first
        emailService.sendContractInvitationEmail(
                event.freelancerEmail(),
                employerName,
                jobTitle,
                event.invitationMessage(),
                event.role(),
                event.agreedRate(),
                event.salaryType(),
                event.startedAt(),
                event.finishedAt()
        );

        // 2. Persist notification in noti DB for candidate
        UUID recipientId = event.employeeId() != null ? event.employeeId() : UUID.randomUUID();
        String details = String.format("Vị trí: %s, Mức lương: %s %s",
                event.role() != null ? event.role() : "N/A",
                event.agreedRate() != null ? event.agreedRate() : "Thoả thuận",
                event.salaryType() != null ? event.salaryType() : "");

        notificationService.createContractInvitationNotification(
                recipientId,
                event.freelancerEmail(),
                employerName,
                jobTitle,
                event.invitationMessage(),
                details
        );
    }

    @RabbitHandler
    public void handleContractAccepted(ContractAcceptedEvent event) {
        log.info("[Notification Contract Listener] Processing ContractAcceptedEvent for contractId: {}, employerId: {}",
                event.contractId(), event.employerId());
        String freelancerName = (event.freelancerName() != null && !event.freelancerName().isBlank())
                ? event.freelancerName()
                : "Ứng viên";

        // 1. Send email to employer
        if (event.employerEmail() != null && !event.employerEmail().isBlank()) {
            emailService.sendContractAcceptedEmail(event.employerEmail(), freelancerName, event.title(), event.contractId());
        }

        // 2. Persist notification in noti DB for employer
        notificationService.createContractAcceptedNotification(
                event.employerId(),
                event.employerEmail(),
                freelancerName,
                event.title()
        );
    }

    @RabbitHandler
    public void handleContractRejected(ContractRejectedEvent event) {
        log.info("[Notification Contract Listener] Processing ContractRejectedEvent for contractId: {}, employerId: {}",
                event.contractId(), event.employerId());
        String freelancerName = (event.freelancerName() != null && !event.freelancerName().isBlank())
                ? event.freelancerName()
                : "Ứng viên";
        String jobTitle = (event.jobTitle() != null && !event.jobTitle().isBlank())
                ? event.jobTitle()
                : "hợp đồng";

        // 1. Send email to employer
        if (event.employerEmail() != null && !event.employerEmail().isBlank()) {
            emailService.sendContractRejectedEmail(event.employerEmail(), freelancerName, jobTitle, event.reason());
        }

        // 2. Persist notification in noti DB for employer
        notificationService.createContractRejectedNotification(
                event.employerId(),
                event.employerEmail(),
                freelancerName,
                jobTitle,
                event.reason()
        );
    }

    @RabbitHandler
    public void handleContractCompensation(ContractCompensationRequestedEvent event) {
        log.warn("[Notification Contract Listener] Processing ContractCompensationRequestedEvent for contractId: {}, reason: {}",
                event.contractId(), event.reason());
        notificationService.createContractCompensatedNotification(event.contractId(), event.reason());
    }

    @RabbitHandler
    public void handleContractCompleted(ContractCompletedEvent event) {
        if (event == null || event.contractId() == null) {
            log.warn("[Notification Contract Listener] Received invalid ContractCompletedEvent");
            return;
        }
        log.info("[Notification Contract Listener] Processing ContractCompletedEvent for contractId: {}", event.contractId());

        String jobTitle = (event.title() != null && !event.title().isBlank()) ? event.title() : "công việc";
        String employerName = (event.employerName() != null && !event.employerName().isBlank()) ? event.employerName() : "Nhà tuyển dụng";

        // 1. Email to freelancer
        if (event.freelancerEmail() != null && !event.freelancerEmail().isBlank()) {
            emailService.sendNotificationEmail(
                    event.freelancerEmail(),
                    "Hợp đồng đã hoàn thành: " + jobTitle,
                    "Hợp đồng đã hoàn thành",
                    "Chúc mừng bạn! Nhà tuyển dụng " + employerName + " đã hoàn thành hợp đồng cho công việc '" + jobTitle + "'."
            );
        }

        // 2. Email to employer
        if (event.employerEmail() != null && !event.employerEmail().isBlank()) {
            emailService.sendNotificationEmail(
                    event.employerEmail(),
                    "Hợp đồng đã hoàn thành: " + jobTitle,
                    "Hợp đồng đã hoàn thành",
                    "Bạn đã đánh dấu hoàn thành hợp đồng cho công việc '" + jobTitle + "'."
            );
        }

        // 3. In-app notifications
        if (event.freelancerId() != null) {
            notificationService.createContractCompletedNotification(event.freelancerId(), event.freelancerEmail(), jobTitle);
        }
        if (event.employerId() != null) {
            notificationService.createContractCompletedNotification(event.employerId(), event.employerEmail(), jobTitle);
        }
    }

    @RabbitHandler
    public void handleContractCancelled(ContractCancelledEvent event) {
        if (event == null || event.contractId() == null) {
            log.warn("[Notification Contract Listener] Received invalid ContractCancelledEvent");
            return;
        }
        log.info("[Notification Contract Listener] Processing ContractCancelledEvent for contractId: {}, cancelledBy: {}",
                event.contractId(), event.cancelledByRole());

        String jobTitle = (event.title() != null && !event.title().isBlank()) ? event.title() : "công việc";
        String reason = (event.reason() != null && !event.reason().isBlank()) ? event.reason() : "Không có lý do";
        boolean cancelledByEmployer = "EMPLOYER".equalsIgnoreCase(event.cancelledByRole());

        if (cancelledByEmployer) {
            // Cancelled by Employer -> Notify Freelancer via Email & In-App
            if (event.freelancerEmail() != null && !event.freelancerEmail().isBlank()) {
                emailService.sendNotificationEmail(
                        event.freelancerEmail(),
                        "Hợp đồng đã bị hủy: " + jobTitle,
                        "Hợp đồng đã bị hủy",
                        "Nhà tuyển dụng đã hủy hợp đồng cho công việc '" + jobTitle + "'. Lý do: " + reason
                );
            }
            if (event.freelancerId() != null) {
                notificationService.createContractCancelledNotification(event.freelancerId(), event.freelancerEmail(), "Nhà tuyển dụng", jobTitle, reason);
            }
            if (event.employerId() != null) {
                notificationService.createContractCancelledNotification(event.employerId(), event.employerEmail(), "Bạn", jobTitle, reason);
            }
        } else {
            // Cancelled by Employee/Freelancer -> Notify Employer via Email & In-App
            if (event.employerEmail() != null && !event.employerEmail().isBlank()) {
                emailService.sendNotificationEmail(
                        event.employerEmail(),
                        "Hợp đồng đã bị hủy: " + jobTitle,
                        "Hợp đồng đã bị hủy",
                        "Ứng viên đã hủy hợp đồng cho công việc '" + jobTitle + "'. Lý do: " + reason
                );
            }
            if (event.employerId() != null) {
                notificationService.createContractCancelledNotification(event.employerId(), event.employerEmail(), "Ứng viên", jobTitle, reason);
            }
            if (event.freelancerId() != null) {
                notificationService.createContractCancelledNotification(event.freelancerId(), event.freelancerEmail(), "Bạn", jobTitle, reason);
            }
        }
    }

    @RabbitHandler
    public void handleReviewCreated(ReviewCreatedEvent event) {
        if (event == null || event.reviewId() == null) {
            log.warn("[Notification Contract Listener] Received invalid ReviewCreatedEvent");
            return;
        }
        log.info("[Notification Contract Listener] Processing ReviewCreatedEvent for reviewId: {}, revieweeId: {}",
                event.reviewId(), event.revieweeId());

        String reviewer = (event.reviewerName() != null && !event.reviewerName().isBlank()) ? event.reviewerName() : "Đối tác";
        int rating = event.rating() != null ? event.rating() : 5;
        String comment = event.comment() != null ? event.comment() : "";

        // 1. Email to reviewee
        if (event.revieweeEmail() != null && !event.revieweeEmail().isBlank()) {
            emailService.sendNotificationEmail(
                    event.revieweeEmail(),
                    "Bạn nhận được đánh giá mới từ " + reviewer,
                    "Đánh giá mới (" + rating + " sao)",
                    reviewer + " đã để lại đánh giá " + rating + " sao cho bạn: \"" + comment + "\""
            );
        }

        // 2. In-app notification to reviewee
        if (event.revieweeId() != null) {
            notificationService.createReviewReceivedNotification(event.revieweeId(), event.revieweeEmail(), reviewer, rating, comment);
        }
    }
}
