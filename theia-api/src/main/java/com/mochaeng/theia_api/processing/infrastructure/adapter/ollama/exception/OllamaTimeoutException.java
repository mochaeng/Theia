package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaTimeoutException extends OllamaException {

    public OllamaTimeoutException(String message) {
        super(message, "OLLAMA_TIMEOUT");
    }

    public OllamaTimeoutException(String message, Throwable cause) {
        super(message, "OLLAMA_TIMEOUT", cause);
    }
}
