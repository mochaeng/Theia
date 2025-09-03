package com.mochaeng.theia_api.processing.domain.model;

import lombok.Builder;

@Builder
public record EmbeddingMetadata(
    String model,
    int tokenCount,
    long processingTimeMs
) {}
