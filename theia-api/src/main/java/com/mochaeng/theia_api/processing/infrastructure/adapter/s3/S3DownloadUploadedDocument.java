package com.mochaeng.theia_api.processing.infrastructure.adapter.s3;

import com.mochaeng.theia_api.processing.application.dto.DownloadDocumentResult;
import com.mochaeng.theia_api.processing.application.port.out.DownloadDocumentPort;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import com.mochaeng.theia_api.shared.infrastructure.s3.S3Properties;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component("s3DownloadUploadedDocument")
@RequiredArgsConstructor
@Slf4j
public class S3DownloadUploadedDocument implements DownloadDocumentPort {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public DownloadDocumentResult download(DocumentUploadedMessage message) {
        log.info(
            "Starting download of document from S3: {}",
            message.bucketPath()
        );

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Properties.bucketName())
            .key(message.bucketPath())
            .build();

        try {
            byte[] documentBytes = s3Client
                .getObject(getObjectRequest, ResponseTransformer.toBytes())
                .asByteArray();

            if (documentBytes.length == 0) {
                return DownloadDocumentResult.failure(
                    DownloadDocumentResult.ErrorCode.EMPTY_DOCUMENT,
                    "Document is empty"
                );
            }

            if (documentBytes.length != message.fileSizeBytes()) {
                return DownloadDocumentResult.failure(
                    DownloadDocumentResult.ErrorCode.INVALID_FILE_SIZE,
                    "Document doesn't match stored file size: have [%d] but stored [%d]".formatted(
                        documentBytes.length,
                        message.fileSizeBytes()
                    )
                );
            }

            log.info("Successfully downloaded document from S3");
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(documentBytes);

            return DownloadDocumentResult.success(documentBytes, hash);
        } catch (NoSuchKeyException e) {
            return DownloadDocumentResult.failure(
                DownloadDocumentResult.ErrorCode.DOCUMENT_NOT_FOUND,
                "Document not found: %s".formatted(e.getMessage())
            );
        } catch (S3Exception e) {
            return DownloadDocumentResult.failure(
                DownloadDocumentResult.ErrorCode.SPECIFIC_ERROR,
                String.format(
                    "failed to download document from S3: %s. Error: %s",
                    message.bucketPath(),
                    e.getMessage()
                )
            );
        } catch (Exception e) {
            return DownloadDocumentResult.failure(
                DownloadDocumentResult.ErrorCode.UNEXPECTED_ERROR,
                "A unexpected error while download happened: Error %s".formatted(
                    e.getMessage()
                )
            );
        }
    }
}
