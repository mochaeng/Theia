package com.mochaeng.theia_api.validator.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;
import io.vavr.control.Option;

public interface FileStoragePort {
    Either<FileStorageError, byte[]> download(String bucket, String key);
    Option<FileStorageError> uploadValidDocument(
        DocumentMessage message,
        byte[] content
    );

    record FileStorageError(String message) {}
}
