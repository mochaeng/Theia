package com.mochaeng.theia_api.query.application.web.dto;

import com.mochaeng.theia_api.shared.domain.TextNormalizer;
import io.vavr.control.Either;
import java.util.Arrays;
import java.util.List;

public record SearchQuery(
    String text,
    List<String> fieldTypes,
    Integer limit,
    Float threshold,
    float[] embedding
) {
    public SearchQuery {
        fieldTypes = fieldTypes != null ? List.copyOf(fieldTypes) : List.of();

        if (embedding != null) {
            embedding = Arrays.copyOf(embedding, embedding.length);
        }
    }

    public static SearchQuery of(
        String query,
        List<String> fieldType,
        Integer limit,
        Float threshold
    ) {
        return new SearchQuery(query, fieldType, limit, threshold, null);
    }

    public SearchQuery withEmbedding(float[] embedding) {
        var embeddingCopy = embedding != null
            ? Arrays.copyOf(embedding, embedding.length)
            : null;
        return new SearchQuery(
            text,
            fieldTypes,
            limit,
            threshold,
            embeddingCopy
        );
    }

    public static Either<SearchQueryError, SearchQuery> validateQuery(
        SearchQuery query
    ) {
        if (TextNormalizer.clean(query.text()).isEmpty()) {
            return Either.left(new SearchQueryError("Invalid text for text"));
        }
        return Either.right(query);
    }

    public record SearchQueryError(String message) {}

    @Override
    public float[] embedding() {
        return embedding != null
            ? Arrays.copyOf(embedding, embedding.length)
            : null;
    }
}
