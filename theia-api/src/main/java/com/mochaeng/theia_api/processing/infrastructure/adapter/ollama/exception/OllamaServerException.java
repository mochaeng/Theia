package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaServerException extends OllamaException {

    public OllamaServerException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "OLLAMA_SERVER_ERROR";
    }
}
