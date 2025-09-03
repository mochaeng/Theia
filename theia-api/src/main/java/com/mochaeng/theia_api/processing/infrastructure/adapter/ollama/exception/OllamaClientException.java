package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaClientException extends OllamaException {

    public OllamaClientException(String message) {
        super(message, "OLLAMA_CLIENT_ERROR");
    }

    public OllamaClientException(String message, Throwable cause) {
        super(message, "OLLAMA_CLIENT_ERROR", cause);
    }
}
