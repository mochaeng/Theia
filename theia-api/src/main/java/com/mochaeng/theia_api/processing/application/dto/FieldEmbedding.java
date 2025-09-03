package com.mochaeng.theia_api.processing.application.dto;

import com.mochaeng.theia_api.processing.domain.model.EmbeddingMetadata;
import lombok.Builder;

@Builder
public record FieldEmbedding(
    String fieldName,
    Float[] embedding,
    String text,
    EmbeddingMetadata metadata
) {
    public boolean hasVector() {
        return embedding != null && embedding.length > 0;
    }

    public int dimensions() {
        return embedding != null ? embedding.length : 0;
    }
}
