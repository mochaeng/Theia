package com.mochaeng.theia_api.validator.application.port.out;

import io.vavr.control.Option;

public interface ValidateDocumentStructurePort {
    Option<StructureError> validate(byte[] content);

    record StructureError(String message) {}
}
