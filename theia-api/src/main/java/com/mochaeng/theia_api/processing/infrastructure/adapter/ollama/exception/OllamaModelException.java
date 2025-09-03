package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaModelException extends OllamaException {

    public OllamaModelException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "OLLAMA_MODEL_ERROR";
    }
}
