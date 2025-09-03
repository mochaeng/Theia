package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public abstract class OllamaException extends RuntimeException {

    protected OllamaException(String message) {
        super(message);
    }

    protected OllamaException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getErrorCode();
}
