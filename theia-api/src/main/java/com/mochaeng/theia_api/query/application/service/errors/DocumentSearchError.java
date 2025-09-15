package com.mochaeng.theia_api.query.application.service.errors;

public sealed interface DocumentSearchError {
    record QueryError(String message, String details) implements
        DocumentSearchError {}

    record UnknownError(String message, String details) implements
        DocumentSearchError {}
}
