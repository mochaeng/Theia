package com.mochaeng.theia_api.query.application.web.dto;

import java.util.List;

public record SearchQuery(
    String query,
    String fieldType,
    Integer limit,
    Float threshold,
    List<Float> embedding
) {
    public static SearchQuery of(
        String query,
        String fieldType,
        Integer limit,
        Float threshold
    ) {
        return new SearchQuery(query, fieldType, limit, threshold, null);
    }

    public SearchQuery withEmbedding(List<Float> embedding) {
        return new SearchQuery(query, fieldType, limit, threshold, embedding);
    }
}
