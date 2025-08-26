package com.mochaeng.theia_api.storage.s3;

import com.mochaeng.theia_api.document.model.Document;
import com.mochaeng.theia_api.shared.config.s3.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {

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

            s3.putObject(request, RequestBody.fromBytes(document.content()));

            log.info("document stored successfully");

            return s3Props.bucketName() + key;
        } catch (Exception e) {
            throw new RuntimeException(
                "error uploading document: " + e.getMessage(),
                e
            );
        }
    }
}
