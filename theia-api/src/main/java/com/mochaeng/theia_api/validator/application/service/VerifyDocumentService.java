package com.mochaeng.theia_api.validator.application.service;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.validator.application.port.in.VerifyDocumentUseCase;
import com.mochaeng.theia_api.validator.application.port.out.*;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerifyDocumentService implements VerifyDocumentUseCase {

    @Value("${storage.s3.validated-bucket-name}")
    private String validatedBucketName;

    private final FileStoragePort storager;
    private final ValidateDocumentStructurePort validator;
    private final VirusScannerPort scanner;
    private final PublishValidatedDocumentPort publisher;

    @Override
    public Option<VerifyDocumentError> verify(DocumentMessage message) {
        log.info("downloading incoming-document '{}'", message.key());
        var documentBytes = storager.download(message.bucket(), message.key());
        if (documentBytes.isLeft()) {
            return error(
                "download of incoming-document failed: '%s'" +
                documentBytes.getLeft().message()
            );
        }

        log.info(
            "checking incoming-document '{}' file structure",
            message.key()
        );
        var validateStructure = validator.validate(documentBytes.get());
        if (!validateStructure.isEmpty()) {
            return error(
                "incoming-document have invalid structure: " +
                validateStructure.get().message()
            );
        }

        log.info("checking incoming-document '{}' for virus", message.key());
        var checkVirus = scanner.scan(documentBytes.get());
        if (checkVirus.isLeft()) {
            return error(
                "virus check failed: %s" + checkVirus.getLeft().message()
            );
        }

        var virusSignature = checkVirus.get().signature();
        if (!virusSignature.isEmpty()) {
            return error(
                "incoming-document contains virus with signature '%s'".formatted(
                    virusSignature.get()
                )
            );
        }

        var validatedMessage = message
            .toBuilder()
            .bucket(validatedBucketName)
            .key(message.documentID().toString())
            .build();

        log.info(
            "uploading incoming-document '{}' to safer bucket",
            message.key()
        );
        var bucketPath = storager.uploadValidDocument(
            validatedMessage,
            documentBytes.get()
        );
        if (!bucketPath.isEmpty()) {
            return error(
                "upload to bucket path '%s' failed: %s".formatted(
                    message.bucket(),
                    bucketPath.get().message()
                )
            );
        }

        var publishDocument = publisher.publish(validatedMessage);
        if (!publishDocument.isEmpty()) {
            return error(
                "validated-document publish message failed: %s" +
                publishDocument.get().message()
            );
        }

        return Option.none();
    }

    private Option<VerifyDocumentError> error(String msg) {
        return Option.some(new VerifyDocumentError(msg));
    }
}
