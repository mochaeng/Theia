package com.mochaeng.theia_api.document.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DocumentProcessedEvent(
    @JsonProperty("documentID") UUID documentID,
    @JsonProperty("title") String title,
    @JsonProperty("authors") List<String> authors,
    @JsonProperty("keywords") List<String> keywords,
    @JsonProperty("abstractText") String abstractText,
    @JsonProperty("vectorEmbedding") List<Float> vectorEmbedding,
    @JsonProperty("metadata") Map<String, Object> metadata,
    @JsonProperty("processedAt") LocalDateTime processedAt
) {
    public DocumentProcessedEvent documentUploadedEvent(
        UUID documentId,
        String title,
        List<String> authors,
        List<String> keywords,
        String abstractText,
        List<Float> vectorEmbedding,
        Map<String, Object> metadata
    ) {
        return new DocumentProcessedEvent(
            documentId,
            title,
            authors,
            keywords,
            abstractText,
            vectorEmbedding,
            metadata,
            LocalDateTime.now()
        );
    }
}
