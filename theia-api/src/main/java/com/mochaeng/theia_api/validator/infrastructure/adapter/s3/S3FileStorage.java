package com.mochaeng.theia_api.validator.infrastructure.adapter.s3;

import com.mochaeng.theia_api.shared.infrastructure.s3.S3Helpers;
import com.mochaeng.theia_api.validator.application.port.out.FileStoragePort;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Component("s3FileStorageValidator")
@RequiredArgsConstructor
public class S3FileStorage implements FileStoragePort {

    private final S3Helpers s3Helpers;

    @Override
    public Either<FileStorageError, byte[]> download(
        String bucket,
        String key
    ) {
        return s3Helpers.download(bucket, key)
            .toEither()
            .mapLeft(this::mapDownloadException);
    }

    @Override
    public Either<FileStorageError, Void> upload(
        String bucket,
        String key,
        String contentType,
        byte[] content
    ) {
        return null;
    }

    private FileStorageError mapDownloadException(Throwable ex) {
        return switch (ex) {
            case NoSuchKeyException ignored -> new FileStorageError("key not found: " + ex.getMessage());
            case AwsServiceException ignored -> new FileStorageError("could not contact aws service: " + ex.getMessage());
            default -> new FileStorageError("s3 exception: " + ex.getMessage());
        };
    }
}
