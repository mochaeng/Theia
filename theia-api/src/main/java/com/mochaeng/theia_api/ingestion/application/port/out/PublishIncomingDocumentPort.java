package com.mochaeng.theia_api.ingestion.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;

public interface PublishIncomingDocumentPort {
    Either<PublishUploadedDocumentError, Void> publishAsync(
        DocumentMessage event
    );
    Either<PublishUploadedDocumentError, Void> publishSync(
        DocumentMessage event
    );

    record PublishUploadedDocumentError(String message) {}
}
