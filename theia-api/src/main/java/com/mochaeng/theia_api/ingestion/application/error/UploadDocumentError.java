package com.mochaeng.theia_api.ingestion.application.error;

public sealed interface UploadDocumentError {
    String message();

    record InvalidInput(String message) implements UploadDocumentError {}

    record Upload(String message) implements UploadDocumentError {}

    record Publish(String message) implements UploadDocumentError {}
}
