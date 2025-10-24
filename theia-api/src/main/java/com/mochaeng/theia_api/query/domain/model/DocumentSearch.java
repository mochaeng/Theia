package com.mochaeng.theia_api.query.domain.model;

import com.mochaeng.theia_api.processing.domain.model.Author;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentSearch(
    UUID documentID,
    String title,
    Float similarity,
    List<Author> authors,
    String filePath,
    Instant createdAt,
    Instant updatedAt
) {
    public DocumentSearch {
        authors = authors != null ? List.copyOf(authors) : List.of();
    }
}
