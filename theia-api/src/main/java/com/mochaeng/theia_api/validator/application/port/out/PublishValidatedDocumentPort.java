package com.mochaeng.theia_api.validator.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.ValidatedDocumentMessage;
import io.vavr.control.Either;

public interface PublishValidatedDocumentPort {
    Either<PublishValidatedDocumentError, Void> publish(
        ValidatedDocumentMessage message
    );

    record PublishValidatedDocumentError(String message) {}
}
