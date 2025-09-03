package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaModelException extends OllamaException {

    public OllamaModelException(String message) {
        super(message, "OLLAMA_MODEL_RESPONSE");
    }

    public OllamaModelException(String message, Throwable cause) {
        super(message, "OLLAMA_MODEL_RESPONSE", cause);
    }
}
