package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaClientException extends OllamaException {

    public OllamaClientException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "OLLAMA_CLIENT_ERROR";
    }
}
