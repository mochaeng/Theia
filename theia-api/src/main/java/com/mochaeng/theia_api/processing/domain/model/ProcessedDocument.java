package com.mochaeng.theia_api.processing.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
    public ProcessedDocument {
        if (fileHash != null) {
            fileHash = Arrays.copyOf(fileHash, fileHash.length);
        }
        fieldEmbeddings = fieldEmbeddings == null
            ? new ArrayList<>()
            : List.copyOf(fieldEmbeddings);
        authors = authors == null ? new ArrayList<>() : List.copyOf(authors);
    }

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

    @Override
    public byte[] fileHash() {
        if (fileHash == null) {
            return null;
        }
        return Arrays.copyOf(fileHash, fileHash.length);
    }

    @Override
    public List<FieldEmbedding> fieldEmbeddings() {
        return new ArrayList<>(fieldEmbeddings);
    }

    @Override
    public List<Author> authors() {
        return new ArrayList<>(authors);
    }
}
