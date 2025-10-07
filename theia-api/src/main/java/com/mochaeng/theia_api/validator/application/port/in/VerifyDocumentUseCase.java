package com.mochaeng.theia_api.validator.application.port.in;

import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import io.vavr.control.Either;

public interface VerifyDocumentUseCase {
    Either<VerifyDocumentError, Void> verify(IncomingDocumentMessage message);

    record VerifyDocumentError(String message) {}
}
