package com.mochaeng.theia_api.processing.domain.model;

import lombok.Builder;

@Builder
public record EmbeddingMetadata(
    String model,
    int dimension,
    int tokenCount,
    long processingTimeMs,
    String textHash
) {}
