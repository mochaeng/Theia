package com.mochaeng.theia_api.validator.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Option;

public interface PublishValidatedDocumentPort {
    Option<PublishValidatedDocumentError> publish(DocumentMessage message);

    record PublishValidatedDocumentError(String message) {}
}
