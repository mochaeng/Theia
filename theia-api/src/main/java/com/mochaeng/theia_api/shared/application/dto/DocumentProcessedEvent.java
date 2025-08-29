package com.mochaeng.theia_api.shared.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.*;

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
    public DocumentProcessedEvent {
        authors = authors != null ? List.copyOf(authors) : List.of();
        keywords = keywords != null ? List.copyOf(keywords) : List.of();
        vectorEmbedding = vectorEmbedding != null
            ? List.copyOf(vectorEmbedding)
            : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

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
            authors != null ? new ArrayList<>(authors) : new ArrayList<>(),
            keywords != null ? new ArrayList<>(keywords) : new ArrayList<>(),
            abstractText,
            vectorEmbedding = vectorEmbedding != null
                ? new ArrayList<>(vectorEmbedding)
                : new ArrayList<>(),
            metadata = metadata != null
                ? new HashMap<>(metadata)
                : new HashMap<>(),
            LocalDateTime.now()
        );
    }
}
