package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaTimeoutException extends OllamaException {

    public OllamaTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "OLLAMA_TIMEOUT";
    }
}
