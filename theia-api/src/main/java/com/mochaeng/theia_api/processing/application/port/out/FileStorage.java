package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;

public interface FileStorage {
    Either<FileStorageError, DownloadResult> download(DocumentMessage message);

    record DownloadResult(byte[] content, byte[] hash) {}

    record FileStorageError(String message) {}
}
