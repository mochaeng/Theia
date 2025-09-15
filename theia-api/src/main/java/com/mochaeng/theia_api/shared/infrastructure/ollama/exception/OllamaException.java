package com.mochaeng.theia_api.shared.infrastructure.ollama.exception;

import lombok.Getter;

@Getter
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
}
