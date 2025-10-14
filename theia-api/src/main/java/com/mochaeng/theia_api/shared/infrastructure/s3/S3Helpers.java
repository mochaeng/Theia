package com.mochaeng.theia_api.shared.infrastructure.s3;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Helpers {

    private final S3Client s3Client;

    public Try<byte[]> download(String bucket, String key) {
        log.info(
            "starting to download document from s3 bucket '{}' with key '{}'",
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
        });
    }

    public Try<PutObjectResponse> store(
        String bucket,
        String key,
        String contentType,
        byte[] content
    ) {
        return Try.of(() -> {
            log.info("storing document '{}' to s3 bucket '{}'", key, bucket);

            var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

            return s3Client.putObject(request, RequestBody.fromBytes(content));
        });
    }
}
