package com.mochaeng.theia_api.validator.application.port.out;

import io.vavr.control.Either;

public interface FileStoragePort {
    Either<FileStorageError, byte[]> download(String bucket, String key);
    Either<FileStorageError, Void> upload(
        String bucket,
        String key,
        String contentType,
        byte[] content
    );

    record FileStorageError(String message) {}
}
