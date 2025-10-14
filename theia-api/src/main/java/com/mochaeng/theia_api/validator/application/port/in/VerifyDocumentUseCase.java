package com.mochaeng.theia_api.validator.application.port.in;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Option;

public interface VerifyDocumentUseCase {
    Option<VerifyDocumentError> verify(DocumentMessage message);

    record VerifyDocumentError(String message) {}
}
