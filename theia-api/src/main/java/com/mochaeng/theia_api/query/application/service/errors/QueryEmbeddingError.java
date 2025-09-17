package com.mochaeng.theia_api.query.application.service.errors;

public sealed interface QueryEmbeddingError extends QueryError {
    String message();

    record ServiceUnavailable(String message) implements QueryEmbeddingError {}

    record InvalidInput(String message) implements QueryEmbeddingError {}

    record ProcessingTimeout(String message) implements QueryEmbeddingError {}

    record InvalidResponse(String message) implements QueryEmbeddingError {}

    record UnknownError(String message) implements QueryEmbeddingError {}
}
