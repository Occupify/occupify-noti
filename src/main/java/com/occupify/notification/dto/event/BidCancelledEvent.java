package com.occupify.notification.dto.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidCancelledEvent(
    UUID bidId,
    UUID jobId,
    UUID projectId,
    UUID employerId,
    UUID freelancerId,
    LocalDateTime cancelledAt
) implements Serializable {}
