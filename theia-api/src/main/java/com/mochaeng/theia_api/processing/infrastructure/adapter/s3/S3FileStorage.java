package com.mochaeng.theia_api.processing.infrastructure.adapter.s3;

import com.mochaeng.theia_api.processing.application.port.out.FileStorage;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.shared.infrastructure.s3.S3Helpers;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3FileStorage implements FileStorage {

    private final S3Helpers s3Helpers;

    @Override
    public Either<FileStorageError, DownloadResult> download(
        DocumentMessage message
    ) {
        log.info(
            "starting download of document '{}' from S3 '{}'",
            message.documentID(),
            message.bucket()
        );

        return s3Helpers
            .download(message.bucket(), message.key())
            .toEither()
            .mapLeft(this::mapDownloadException)
            .flatMap(this::createDownloadResult);
    }

    private Either<FileStorageError, DownloadResult> createDownloadResult(
        byte[] content
    ) {
        return computeHash(content)
            .toEither()
            .mapLeft(ex ->
                new FileStorageError("hash failed: " + ex.getMessage())
            )
            .map(hash -> new DownloadResult(content, hash));
    }

    private Try<byte[]> computeHash(byte[] content) {
        return Try.of(() -> {
            var digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(content);
        });
    }

    private FileStorageError mapDownloadException(Throwable ex) {
        return switch (ex) {
            case NoSuchKeyException ignored -> new FileStorageError(
                "document not found: " + ex.getMessage()
            );
            case S3Exception ignored -> new FileStorageError(
                "s3 exception: " + ex.getMessage()
            );
            default -> new FileStorageError(
                "unexpected exception: " + ex.getMessage()
            );
        };
    }
}
