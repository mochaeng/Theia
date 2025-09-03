package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception;

public class GrobidUnavailableException extends GrobidException {

    public GrobidUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "GROBID_UNAVAILABLE";
    }
}
