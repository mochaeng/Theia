package com.mochaeng.theia_api.ingestion.application.port.in;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import io.vavr.control.Either;

public interface UploadIncomingDocumentUseCase {
    Either<UploadIncomingDocumentError, IncomingDocumentMessage> upload(
        Document document
    );

    record UploadIncomingDocumentError(String message) {}
}
