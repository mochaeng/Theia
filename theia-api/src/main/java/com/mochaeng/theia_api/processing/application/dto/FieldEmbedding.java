package com.mochaeng.theia_api.processing.application.dto;

import com.mochaeng.theia_api.processing.domain.model.DocumentField;
import com.mochaeng.theia_api.processing.domain.model.EmbeddingMetadata;
import lombok.Builder;

@Builder
public record FieldEmbedding(
    DocumentField fieldName,
    Float[] embedding,
    String text,
    EmbeddingMetadata metadata
) {
    public FieldEmbedding {
        embedding = embedding == null ? null : embedding.clone();
    }

    public boolean hasEmbedding() {
        return embedding != null && embedding.length > 0;
    }

    public int dimensions() {
        return embedding != null ? embedding.length : 0;
    }

    @Override
    public Float[] embedding() {
        return embedding == null ? null : embedding.clone();
    }
}
