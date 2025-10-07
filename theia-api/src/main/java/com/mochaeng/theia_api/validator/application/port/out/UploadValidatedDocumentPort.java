package com.mochaeng.theia_api.validator.application.port.out;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import io.vavr.control.Either;

public interface UploadValidatedDocumentPort {
    Either<UploadValidatedDocumentError, String> upload(
        IncomingDocumentMessage incomingMessage,
        byte[] content
    );

    record UploadValidatedDocumentError(String message) {}
}
