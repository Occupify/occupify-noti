package com.occupify.notification.listener;

import com.occupify.notification.dto.event.BidCancelledEvent;
import com.occupify.notification.dto.event.BidCreatedEvent;
import com.occupify.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${app.rabbitmq.bid-queue:occupify.notification.bid.queue}")
@RequiredArgsConstructor
@Slf4j
public class BidEventListener {

    private final NotificationService notificationService;

    @RabbitHandler
    public void handleBidCreatedEvent(BidCreatedEvent event) {
        log.info("[Event Listener] Processing bid created event for bid [{}], job ['{}'], employer [{}]",
                event.bidId(), event.jobTitle(), event.employerId());
        notificationService.processBidCreatedEvent(event);
    }

    @RabbitHandler
    public void handleBidCancelledEvent(BidCancelledEvent event) {
        log.info("[Event Listener] Processing bid cancelled event for bid [{}], employer [{}]",
                event.bidId(), event.employerId());
        notificationService.processBidCancelledEvent(event);
    }
}
