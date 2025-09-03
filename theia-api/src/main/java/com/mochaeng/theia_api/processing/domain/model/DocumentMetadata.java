package com.mochaeng.theia_api.processing.domain.model;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentMetadata(
    UUID documentId,
    String title,
    String author,
    String abstractText,
    Map<String, Object> additionalMetadata
) {
    public DocumentMetadata {
        additionalMetadata = additionalMetadata == null
            ? null
            : Map.copyOf(additionalMetadata);
    }
}
