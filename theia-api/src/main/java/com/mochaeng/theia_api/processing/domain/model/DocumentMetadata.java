package com.mochaeng.theia_api.processing.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentMetadata(
    UUID documentId,
    String title,
    String abstractText,
    Set<Author> authors,
    Map<String, Object> additionalMetadata
) {
    public DocumentMetadata {
        additionalMetadata = additionalMetadata == null
            ? null
            : Map.copyOf(additionalMetadata);
    }
}
