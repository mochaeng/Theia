package com.mochaeng.theia_api.shared.infrastructure.s3;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Helpers {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public record S3DownloadError(String message) {}

    public record S3UploadError(String message) {}

    public record S3DownloadResult(byte[] content) {}

    public Either<S3DownloadError, S3DownloadResult> download(
        String bucket,
        String key
    ) {
        log.info(
            "starting to download document from '{}' with key '{}'",
            bucket,
            key
        );

        return Try.of(() -> {
            var request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            return s3Client
                .getObject(request, ResponseTransformer.toBytes())
                .asByteArray();
        })
            .toEither()
            .mapLeft(this::mapDownloadException)
            .map(S3DownloadResult::new);
    }

    public Option<S3UploadError> storeDocument(
        String bucket,
        String key,
        String contentType,
        byte[] content
    ) {
        return Try.run(() -> {
            log.info("storing document '{}' to s3 bucket '{}'", key, bucket);

            var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

            var response = s3Client.putObject(request, RequestBody.fromBytes(content));
        })
            .toEither()
            .mapLeft(this::mapUploadException)
            .fold(
                Option::some,
                $ -> Option.none()
            );
    }

    private S3DownloadError mapDownloadException(Throwable e) {
        return switch (e) {
            case NoSuchKeyException ex -> new S3DownloadError(
                "document not found: " + ex.getMessage()
            );
            case S3Exception ex -> new S3DownloadError(
                "s3 error: " + ex.getMessage()
            );
            default -> new S3DownloadError(
                "unexpected error: " + e.getMessage()
            );
        };
    }

    private S3UploadError mapUploadException(Throwable e) {
        return switch (e) {
            case S3Exception ex -> new S3UploadError(
                "s3 error: " + ex.getMessage()
            );
            default -> new S3UploadError(
                "unexpected error: " + e.getMessage()
            );
        };
    }
}
