package com.mochaeng.theia_api.shared.infrastructure.ollama.exception;

public class OllamaInvalidResponse extends OllamaException {

    public OllamaInvalidResponse(String message) {
        super(message, "OLLAMA_INVALID_RESPONSE");
    }

    public OllamaInvalidResponse(String message, Throwable cause) {
        super(message, "OLLAMA_INVALID_RESPONSE", cause);
    }
}
