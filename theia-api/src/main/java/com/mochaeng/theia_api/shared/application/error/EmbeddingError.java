package com.mochaeng.theia_api.shared.application.error;

public sealed interface EmbeddingError {
    String message();

    record UnavailableService(String message) implements EmbeddingError {}

    record InvalidInput(String message) implements EmbeddingError {}

    record ProcessingTimeout(String message) implements EmbeddingError {}

    record InvalidResponse(String message) implements EmbeddingError {}

    record UnknownError(String message) implements EmbeddingError {}
}
