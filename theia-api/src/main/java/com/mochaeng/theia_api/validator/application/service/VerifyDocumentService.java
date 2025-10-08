package com.mochaeng.theia_api.validator.application.service;

import com.mochaeng.theia_api.shared.application.dto.IncomingDocumentMessage;
import com.mochaeng.theia_api.shared.application.dto.ValidatedDocumentMessage;
import com.mochaeng.theia_api.validator.application.port.in.VerifyDocumentUseCase;
import com.mochaeng.theia_api.validator.application.port.out.*;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    public Either<VerifyDocumentError, Void> verify(
        IncomingDocumentMessage incomingMessage
    ) {
        // 1. download file from s3
        // 2. validate structure
        // 3. check for virus
        // 4. upload to validated bucket
        // 5. publish event

        var documentBytes = storager.download(
            incomingMessage.bucket(),
            incomingMessage.key()
        );
        if (documentBytes.isLeft()) {
            return Either.left(
                new VerifyDocumentError(
                    "failed to download document from bucket: " +
                    documentBytes.getLeft().message()
                )
            );
        }

        var validateStructure = validator.validate(documentBytes.get());
        if (!validateStructure.isEmpty()) {
            return Either.left(
                new VerifyDocumentError(
                    "failed to validate document structure: " +
                    validateStructure.get().message()
                )
            );
        }

        var checkVirus = scanner.scan(documentBytes.get());
        if (checkVirus.isLeft()) {
            return Either.left(
                new VerifyDocumentError(
                    "failed to check document '%s' for virus: %s".formatted(
                        incomingMessage.documentID(),
                        checkVirus.getLeft().message()
                    )
                )
            );
        }

        var virusSignature = checkVirus.get().signature();
        if (!virusSignature.isEmpty()) {
            return Either.left(
                new VerifyDocumentError(
                    "failed to validate document: '%s' contains virus with signature '%s'".formatted(
                        incomingMessage.documentID(),
                        virusSignature
                    )
                )
            );
        }

        var bucketPath = storager.upload(
            validatedBucketName,
            incomingMessage.documentID().toString(),
            incomingMessage.contentType(),
            documentBytes.get()
        );
        if (bucketPath.isLeft()) {
            return Either.left(
                new VerifyDocumentError(
                    "failed to upload document to bucket path '%s': %s".formatted(
                        incomingMessage.bucket(),
                        bucketPath.getLeft().message()
                    )
                )
            );
        }

        var validatedMessage = ValidatedDocumentMessage.from(
            "",
            "",
            incomingMessage
        );
        var publishDocument = publisher.publish(validatedMessage);
        if (publishDocument.isLeft()) {
            return Either.left(
                new VerifyDocumentError(
                    "failed to publish document '%s' message: %s".formatted(
                        incomingMessage.documentID(),
                        publishDocument.getLeft().message()
                    )
                )
            );
        }

        return Either.right(null);
    }
}
