package com.mochaeng.theia_api.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadedMessage(
    @JsonProperty("documentID") UUID documentID,
    @JsonProperty("filename") String filename,
    @JsonProperty("bucketPath") String bucketPath,
    @JsonProperty("contentType") String contentType,
    @JsonProperty("fileSizeBytes") long fileSizeBytes,
    @JsonProperty("uploadedAt") LocalDateTime uploadedAt
) {
    public static DocumentUploadedMessage create(
        UUID documentID,
        String filename,
        String bucketPath,
        String contentType,
        long fileSizeBytes
    ) {
        return new DocumentUploadedMessage(
            documentID,
            filename,
            bucketPath,
            contentType,
            fileSizeBytes,
            LocalDateTime.now()
        );
    }
}
