package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;
import java.util.Arrays;

public interface FileStorage {
    Either<FileStorageError, DownloadResult> download(DocumentMessage message);

    record DownloadResult(byte[] content, byte[] hash) {
        public DownloadResult {
            content = content != null
                ? Arrays.copyOf(content, content.length)
                : new byte[0];
            hash = hash != null
                ? Arrays.copyOf(hash, hash.length)
                : new byte[0];
        }

        @Override
        public byte[] content() {
            return Arrays.copyOf(content, content.length);
        }

        @Override
        public byte[] hash() {
            return Arrays.copyOf(hash, hash.length);
        }
    }

    record FileStorageError(String message) {}
}
