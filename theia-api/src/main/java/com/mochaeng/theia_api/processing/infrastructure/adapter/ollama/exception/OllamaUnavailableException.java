package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaUnavailableException extends OllamaException {

    public OllamaUnavailableException(String message) {
        super(message, "OLLAMA_UNAVAILABLE");
    }

    public OllamaUnavailableException(String message, Throwable cause) {
        super(message, "OLLAMA_UNAVAILABLE", cause);
    }

    @Override
    public String getErrorCode() {
        return "OLLAMA_UNAVAILABLE";
    }
}
