package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaServerException extends OllamaException {

    public OllamaServerException(String message) {
        super(message, "OLLAMA_SERVER_ERROR");
    }

    public OllamaServerException(String message, Throwable cause) {
        super(message, "OLLAMA_SERVER_ERROR", cause);
    }
}
