package com.mochaeng.theia_api.processing.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record ProcessedDocument(
    UUID id,
    String title,
    String abstractText,
    Float[] titleEmbedding,
    Float[] abstractEmbedding,
    Instant createdAt,
    Instant updatedAt
) {
    public static ProcessedDocument from(
        DocumentMetadata metadata,
        DocumentEmbeddings embeddings
    ) {
        Float[] titleEmbedding = null;
        Float[] abstractEmbedding = null;

        for (FieldEmbedding fieldEmbedding : embeddings.fieldEmbeddings()) {
            if (fieldEmbedding.fieldName() == DocumentField.TITLE) {
                titleEmbedding = fieldEmbedding.embedding();
            } else if (fieldEmbedding.fieldName() == DocumentField.ABSTRACT) {
                abstractEmbedding = fieldEmbedding.embedding();
            }
        }

        return ProcessedDocument.builder()
            .id(metadata.documentId())
            .title(metadata.title())
            .abstractText(metadata.abstractText())
            .titleEmbedding(titleEmbedding)
            .abstractEmbedding(abstractEmbedding)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    public boolean hasTitleEmbedding() {
        return titleEmbedding != null && titleEmbedding.length > 0;
    }

    public boolean hasAbstractEmbedding() {
        return abstractEmbedding != null && abstractEmbedding.length > 0;
    }
}
