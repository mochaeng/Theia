package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception;

public class GrobidParsingException extends GrobidException {

    public GrobidParsingException(String message) {
        super(message);
    }

    public GrobidParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "GROBID_PARSING_ERROR";
    }
}
