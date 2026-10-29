package com.occupify.notification.listener;

import com.occupify.notification.dto.event.ProjectClosedEvent;
import com.occupify.notification.dto.event.ProjectCreatedEvent;
import com.occupify.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${app.rabbitmq.project-queue:occupify.notification.project.queue}")
@RequiredArgsConstructor
@Slf4j
public class ProjectEventListener {

    private final NotificationService notificationService;

    @RabbitHandler
    public void handleProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("[Event Listener] Processing project created event for project [{}] ('{}'), employer [{}]",
                event.projectId(), event.title(), event.employerId());
        notificationService.processProjectCreatedEvent(event);
    }

    @RabbitHandler
    public void handleProjectClosedEvent(ProjectClosedEvent event) {
        log.info("[Event Listener] Processing project closed event for project [{}] ('{}'), employer [{}]",
                event.projectId(), event.title(), event.employerId());
        notificationService.processProjectClosedEvent(event);
    }
}
