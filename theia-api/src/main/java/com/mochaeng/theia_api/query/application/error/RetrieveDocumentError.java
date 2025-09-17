package com.mochaeng.theia_api.query.application.error;

public sealed interface RetrieveDocumentError {
    String message();

    record ServiceUnavailableError(String message) implements
        RetrieveDocumentError {}
}
