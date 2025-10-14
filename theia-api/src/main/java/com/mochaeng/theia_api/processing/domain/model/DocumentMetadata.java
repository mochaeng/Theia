package com.mochaeng.theia_api.processing.domain.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentMetadata(
    //    UUID documentId,
    String title,
    String abstractText,
    List<Author> authors,
    Map<String, Object> additionalMetadata
) {
    public DocumentMetadata {
        authors = authors == null ? null : List.copyOf(authors);

        additionalMetadata = additionalMetadata == null
            ? null
            : Map.copyOf(additionalMetadata);
    }
}
