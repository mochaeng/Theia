package com.mochaeng.theia_api.ingestion.application.port.in;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import io.vavr.control.Either;

public interface AcceptDocumentUseCase {
    Either<AcceptDocumentError, Void> validate(Document document);

    record AcceptDocumentError(String message) {}
}
