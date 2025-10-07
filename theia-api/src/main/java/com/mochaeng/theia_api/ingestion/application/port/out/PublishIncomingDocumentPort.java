package com.mochaeng.theia_api.ingestion.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import io.vavr.control.Either;

public interface PublishIncomingDocumentPort {
    Either<PublishUploadedDocumentError, Void> publishAsync(
        IncomingDocumentMessage event
    );
    Either<PublishUploadedDocumentError, Void> publishSync(
        IncomingDocumentMessage event
    );

    record PublishUploadedDocumentError(String message) {}
}
