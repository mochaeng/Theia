package com.mochaeng.theia_api.ingestion.application.port.in;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import io.vavr.control.Try;

public interface ValidateDocumentUseCase {
    Try<Void> validate(Document document);
}
