package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception;

public class GrobidServerException extends GrobidException {

    public GrobidServerException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "GROBID_SERVER_ERROR";
    }
}
