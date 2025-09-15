package com.mochaeng.theia_api.shared.infrastructure.ollama;

public sealed interface OllamaError {
    record Timeout(String message, String details) implements OllamaError {}

    record Unavailable(String message, String details) implements OllamaError {}

    record InvalidResponse(String message, String details) implements
        OllamaError {}

    record NetworkError(String message, String details) implements
        OllamaError {}

    record InvalidInput(String message, String details) implements
        OllamaError {}

    record UnknownError(String message, String details) implements
        OllamaError {}
}
