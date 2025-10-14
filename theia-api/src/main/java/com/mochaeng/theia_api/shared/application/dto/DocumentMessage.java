package com.mochaeng.theia_api.shared.application.dto;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentMessage(
    UUID documentID,
    String userID,
    String bucket,
    String key,
    String contentType,
    long fileSizeBytes,
    Instant uploadedAt
) {
    public static DocumentMessage create(
        Document document,
        String bucket,
        String key
    ) {
        return new DocumentMessage(
            document.id(),
            document.userID(),
            bucket,
            key,
            document.contentType(),
            document.content().length,
            Instant.now()
        );
    }
}
