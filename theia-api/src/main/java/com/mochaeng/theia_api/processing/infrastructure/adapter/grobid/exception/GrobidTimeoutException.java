package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception;

public class GrobidTimeoutException extends GrobidException {

    public GrobidTimeoutException(String message) {
        super(message);
    }

    public GrobidTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "GROBID_TIMEOUT_ERROR";
    }
}
