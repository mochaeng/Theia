package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception;

public class GrobidClientException extends GrobidException {

    public GrobidClientException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "GROBID_CLIENT_ERROR";
    }
}
