package com.mochaeng.theia_api.ingestion.infrastructure.adapter.s3;

import com.mochaeng.theia_api.ingestion.application.port.out.FileStoragePort;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.infrastructure.s3.S3Properties;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component("s3FileStorage")
@RequiredArgsConstructor
@Slf4j
public class S3FileStorage implements FileStoragePort {

    private final S3Client s3;
    private final S3Properties s3Props;

    @Override
    public String storeDocument(Document document) {
        try {
            log.info("Storing document: {}", document);

            String key = "/incoming/" + document.filename();

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Props.bucketName())
                .key(key)
                .contentType(document.contentType())
                .build();

            s3.putObject(
                request,
                RequestBody.fromBytes(
                    Objects.requireNonNull(document.content())
                )
            );

            log.info("document stored successfully");

            return key;
        } catch (Exception e) {
            throw new RuntimeException(
                "error uploading document: " + e.getMessage(),
                e
            );
        }
    }
}
