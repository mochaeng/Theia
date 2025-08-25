package com.mochaeng.theia_api.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadedEvent(
    @JsonProperty("documentID") UUID documentID,
    @JsonProperty("filename") String filename,
    @JsonProperty("bucketPath") String bucketPath,
    @JsonProperty("contentType") String contentType,
    @JsonProperty("fileSizeBytes") long fileSizeBytes,
    @JsonProperty("uploadedAt") LocalDateTime uploadedAt
) {
    public static DocumentUploadedEvent create(
        UUID documentID,
        String filename,
        String bucketPath,
        String contentType,
        long fileSizeBytes
    ) {
        return new DocumentUploadedEvent(
            documentID,
            filename,
            bucketPath,
            contentType,
            fileSizeBytes,
            LocalDateTime.now()
        );
    }
}
