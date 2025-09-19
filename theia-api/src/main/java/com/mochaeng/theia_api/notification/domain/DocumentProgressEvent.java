package com.mochaeng.theia_api.notification.domain;

import java.time.Instant;
import java.util.UUID;

public record DocumentProgressEvent(
    UUID documentID,
    String status,
    String message,
    Integer percentage,
    Instant occurredAt
) {
    public static DocumentProgressEvent now(
        UUID documentID,
        String status,
        String message,
        Integer percentage
    ) {
        return new DocumentProgressEvent(
            documentID,
            status,
            message,
            percentage,
            Instant.now()
        );
    }
}
