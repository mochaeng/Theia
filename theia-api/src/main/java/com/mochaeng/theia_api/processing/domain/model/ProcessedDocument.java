package com.mochaeng.theia_api.processing.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record ProcessedDocument(
    UUID id,
    byte[] fileHash,
    String filePath,
    List<FieldEmbedding> fieldEmbeddings,
    List<Author> authors,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProcessedDocument from(
        DocumentMetadata metadata,
        byte[] fileHash,
        String filePath,
        DocumentEmbeddings embeddings
    ) {
        return ProcessedDocument.builder()
            .id(metadata.documentId())
            .fileHash(fileHash)
            .filePath(filePath)
            .fieldEmbeddings(embeddings.fieldEmbeddings())
            .authors(metadata.authors())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    public boolean hasEmbeddings() {
        return fieldEmbeddings != null && !fieldEmbeddings.isEmpty();
    }
}
