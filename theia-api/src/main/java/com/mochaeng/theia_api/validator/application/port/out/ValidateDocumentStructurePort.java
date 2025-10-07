package com.mochaeng.theia_api.validator.application.port.out;

import io.vavr.control.Option;
import java.time.Instant;

public interface ValidateDocumentStructurePort {
    Option<StructureError> validate(byte[] content);

    record ValidateResult(String tool, Instant totalTime) {}

    record StructureError(String message) {}
}
