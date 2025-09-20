package com.mochaeng.theia_api.notification.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

public record DocumentProgressEvent(
    UUID documentID,
    Status status,
    String message,
    Integer percentage,
    Instant occurredAt
) {
    @RequiredArgsConstructor
    public enum Status {
        FAILED("Failed to process your document", 0),
        DOWNLOADING("Start to downloading your file", 10),
        EXTRACTING("Extracting document information", 40),
        EMBEDDING("Generating embeddings for your document", 70),
        SAVING("Saving data", 90),
        COMPLETED("Completed", 100);

        private final String message;
        private final int percentage;
    }

    public static DocumentProgressEvent now(UUID documentID, Status status) {
        return new DocumentProgressEvent(
            documentID,
            status,
            status.message,
            status.percentage,
            Instant.now()
        );
    }
}
