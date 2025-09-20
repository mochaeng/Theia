package com.mochaeng.theia_api.ingestion.application.port.in;

import com.mochaeng.theia_api.ingestion.application.error.UploadDocumentError;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import io.vavr.control.Either;

public interface UploadDocumentUseCase {
    Either<UploadDocumentError, DocumentUploadedMessage> uploadDocument(
        Document document
    );
}
