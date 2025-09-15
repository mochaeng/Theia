package com.mochaeng.theia_api.shared.application.error;

public sealed interface SimilaritySearchError {
    record ServiceUnavailableError(String message, String details) implements
        SimilaritySearchError {}
}
