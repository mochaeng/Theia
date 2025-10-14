package com.mochaeng.theia_api.validator.infrastructure.adapter.s3;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.shared.infrastructure.s3.S3Helpers;
import com.mochaeng.theia_api.validator.application.port.out.FileStoragePort;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Slf4j
@Component("s3FileStorageValidator")
@RequiredArgsConstructor
public class S3FileStorage implements FileStoragePort {

    private final S3Helpers s3Helpers;

    @Override
    public Either<FileStorageError, byte[]> download(
        String bucket,
        String key
    ) {
        return s3Helpers
            .download(bucket, key)
            .toEither()
            .mapLeft(this::mapDownloadException);
    }

    @Override
    public Option<FileStorageError> uploadValidDocument(
        DocumentMessage message,
        byte[] content
    ) {
        return s3Helpers
            .store(
                message.bucket(),
                message.key(),
                message.contentType(),
                content
            )
            .onFailure(ex ->
                log.info(
                    "failed to upload '{} to bucket '{}': {}",
                    message.key(),
                    message.bucket(),
                    ex.getMessage()
                )
            )
            .fold(
                ex -> Option.some(mapUploadException(ex)),
                $ -> Option.none()
            );
    }

    private FileStorageError mapDownloadException(Throwable ex) {
        return switch (ex) {
            case NoSuchKeyException ignored -> new FileStorageError(
                "key not found: " + ex.getMessage()
            );
            case AwsServiceException ignored -> new FileStorageError(
                "could not contact aws service: " + ex.getMessage()
            );
            default -> new FileStorageError("s3 exception: " + ex.getMessage());
        };
    }

    private FileStorageError mapUploadException(Throwable ex) {
        return switch (ex) {
            case AwsServiceException ignored -> new FileStorageError(
                "could not contact aws service: " + ex.getMessage()
            );
            default -> new FileStorageError("s3 exception: " + ex.getMessage());
        };
    }
}
