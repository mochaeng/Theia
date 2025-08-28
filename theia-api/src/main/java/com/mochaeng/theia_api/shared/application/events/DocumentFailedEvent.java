package com.mochaeng.theia_api.shared.application.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentFailedEvent(
    @JsonProperty("documentID") UUID documentID,
    @JsonProperty("errorCode") String errorCode,
    @JsonProperty("errorMessage") String errorMessage,
    @JsonProperty("failedAt") LocalDateTime failedAt,
    @JsonProperty("retryCount") int retryCount
) {
    public static DocumentFailedEvent create(
        UUID documentId,
        String errorCode,
        String errorMessage,
        int retryCount
    ) {
        return new DocumentFailedEvent(
            documentId,
            errorCode,
            errorMessage,
            LocalDateTime.now(),
            retryCount
        );
    }
}
