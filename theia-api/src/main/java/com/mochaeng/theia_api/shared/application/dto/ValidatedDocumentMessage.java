package com.mochaeng.theia_api.shared.application.dto;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValidatedDocumentMessage(
    UUID documentID,
    String userID,
    String bucket,
    String key,
    //    String bucketPath,
    String contentType,
    long fileSizeBytes,
    LocalDateTime uploadedAt
) {
    public static ValidatedDocumentMessage from(
        String bucket,
        String key,
        IncomingDocumentMessage message
        //        String bucketPath
    ) {
        return new ValidatedDocumentMessage(
            message.documentID(),
            message.userID(),
            bucket,
            key,
            //            bucketPath,
            message.contentType(),
            message.fileSizeBytes(),
            LocalDateTime.now()
        );
    }
}
