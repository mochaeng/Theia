package com.mochaeng.theia_api.shared.application.error;

public sealed interface EmbeddingGenerationError {
    record ServiceUnavailable(String message, String details) implements
        EmbeddingGenerationError {}

    record InvalidInput(String message, String details) implements
        EmbeddingGenerationError {}

    record ProcessingTimeout(String message, String details) implements
        EmbeddingGenerationError {}

    record InvalidResponse(String message, String details) implements
        EmbeddingGenerationError {}

    record QuotaExceeded(String message, String details) implements
        EmbeddingGenerationError {}

    record UnknownError(String message, String details) implements
        EmbeddingGenerationError {}
}
