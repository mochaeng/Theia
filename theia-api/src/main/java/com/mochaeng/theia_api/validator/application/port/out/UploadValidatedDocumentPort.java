package com.mochaeng.theia_api.validator.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;

public interface UploadValidatedDocumentPort {
    Either<UploadValidatedDocumentError, String> upload(
        DocumentMessage incomingMessage,
        byte[] content
    );

    record UploadValidatedDocumentError(String message) {}
}
