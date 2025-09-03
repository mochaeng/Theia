package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaException extends RuntimeException {

    private final String errorCode;

    public OllamaException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public OllamaException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
