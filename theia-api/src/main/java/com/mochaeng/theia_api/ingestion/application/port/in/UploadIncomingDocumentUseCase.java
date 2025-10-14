package com.mochaeng.theia_api.ingestion.application.port.in;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;

public interface UploadIncomingDocumentUseCase {
    Either<UploadError, DocumentMessage> upload(Document document);

    record UploadError(String message) {}
}
