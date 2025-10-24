package com.mochaeng.theia_api.processing.domain.model;

import java.util.Collections;
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
    List<Keyword> keywords,
    Map<String, Object> additionalMetadata
) {
    public DocumentMetadata {
        authors = authors == null ? null : List.copyOf(authors);

        keywords = keywords == null
            ? Collections.emptyList()
            : List.copyOf(keywords);

        additionalMetadata = additionalMetadata == null
            ? null
            : Map.copyOf(additionalMetadata);
    }

    @Override
    public List<Author> authors() {
        return List.copyOf(authors);
    }

    @Override
    public List<Keyword> keywords() {
        return List.copyOf(keywords);
    }
}
