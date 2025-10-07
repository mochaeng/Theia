package com.mochaeng.theia_api.shared.application.dto;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import java.time.Instant;
import java.util.UUID;

public record IncomingDocumentMessage(
    UUID documentID,
    String userID,
    String bucket,
    String key,
    String contentType,
    long fileSizeBytes,
    Instant uploadedAt
) {
    public static IncomingDocumentMessage create(
        Document document,
        String bucket,
        String key
    ) {
        return new IncomingDocumentMessage(
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
