package com.mochaeng.theia_api.query.application.service.exceptions;

import lombok.Getter;

@Getter
public class SearchDocumentException extends RuntimeException {

    public enum Code {
        QUERY_ERROR,
        SIMILARITY_ERROR,
    }

    private final Code code;

    public SearchDocumentException(Code code, String message) {
        super(message);
        this.code = code;
    }
}
