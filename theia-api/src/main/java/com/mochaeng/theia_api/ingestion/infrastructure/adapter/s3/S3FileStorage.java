package com.mochaeng.theia_api.ingestion.infrastructure.adapter.s3;

import com.mochaeng.theia_api.ingestion.application.port.out.FileStoragePort;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.infrastructure.s3.S3Properties;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component("s3FileStorageIngestion")
@RequiredArgsConstructor
@Slf4j
public class S3FileStorage implements FileStoragePort {

    private final S3Client s3;

    @Override
    public Either<FileStoreError, String> storeDocument(
        String bucket,
        String key,
        Document document
    ) {
        return Try.of(() -> {
            log.info("storing document '{}'", document);

            var request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(document.contentType())
                .build();

            s3.putObject(request, RequestBody.fromBytes(document.content()));

            return key;
        })
            .toEither()
            .mapLeft(ex ->
                new FileStoreError(
                    "failed to upload document: " + ex.getMessage()
                )
            );
    }
}
