package com.occupify.notification.dto.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectClosedEvent(
    UUID projectId,
    String title,
    UUID employerId,
    String employerEmail,
    LocalDateTime closedAt
) implements Serializable {}
