package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.exception;

public class OllamaInvalidResponse extends OllamaException {

    public OllamaInvalidResponse(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "OLLAMA_INVALID_RESPONSE";
    }
}
