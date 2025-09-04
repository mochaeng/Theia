package com.mochaeng.theia_api.processing.domain.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentEmbeddings(
    UUID documentId,
    List<FieldEmbedding> fieldEmbeddings
) {
    public DocumentEmbeddings {
        fieldEmbeddings = fieldEmbeddings == null
            ? List.of()
            : List.copyOf(fieldEmbeddings);
    }

    public boolean hasEmbeddings() {
        return fieldEmbeddings != null && !fieldEmbeddings.isEmpty();
    }
}
