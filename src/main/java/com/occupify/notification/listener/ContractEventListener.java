package com.occupify.notification.listener;

import com.occupify.notification.dto.event.ContractAcceptedEvent;
import com.occupify.notification.dto.event.ContractCompensationRequestedEvent;
import com.occupify.notification.dto.event.ContractCreatedEvent;
import com.occupify.notification.dto.event.ContractRejectedEvent;
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
}
