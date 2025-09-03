package com.mochaeng.theia_api.processing.application.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentEmbedding(
    UUID documentId,
    List<FieldEmbedding> fieldEmbeddings
) {
    public boolean hasEmbeddings() {
        return fieldEmbeddings != null && !fieldEmbeddings.isEmpty();
    }
}
