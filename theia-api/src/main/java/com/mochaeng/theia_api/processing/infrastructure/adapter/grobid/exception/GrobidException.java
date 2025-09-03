package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception;

public abstract class GrobidException extends RuntimeException {

    protected GrobidException(String message) {
        super(message);
    }

    protected GrobidException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getErrorCode();
}
