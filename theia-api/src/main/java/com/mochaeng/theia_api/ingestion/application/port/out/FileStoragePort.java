package com.mochaeng.theia_api.ingestion.application.port.out;

import com.mochaeng.theia_api.ingestion.domain.model.Document;
import io.vavr.control.Either;

public interface FileStoragePort {
    Either<FileStoreError, String> storeDocument(
        String bucket,
        String key,
        Document document
    );

    record FileStoreError(String message) {}
}
