package com.mochaeng.theia_api.query.application.web.dto;

import com.mochaeng.theia_api.query.application.service.errors.DocumentSearchError;
import com.mochaeng.theia_api.shared.domain.TextNormalizer;
import io.vavr.control.Either;

public record SearchQuery(
    String query,
    String fieldType,
    Integer limit,
    Float threshold,
    float[] embedding
) {
    public static SearchQuery of(
        String query,
        String fieldType,
        Integer limit,
        Float threshold
    ) {
        return new SearchQuery(query, fieldType, limit, threshold, null);
    }

    public SearchQuery withEmbedding(float[] embedding) {
        return new SearchQuery(query, fieldType, limit, threshold, embedding);
    }

    public static Either<DocumentSearchError, SearchQuery> validateQuery(
        SearchQuery query
    ) {
        if (TextNormalizer.forNomic(query.query()).isEmpty()) {
            return Either.left(
                new DocumentSearchError.InvalidInputError(
                    "Invalid text for query"
                )
            );
        }
        return Either.right(query);
    }
}
