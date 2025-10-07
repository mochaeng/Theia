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
        //        return validateStructure(content)
        //            .mapLeft(ex ->
        //                new ValidationError(
        //                    "failed to validate document structure: " + ex.
        //
        //
        //
        //                    msg()
        //                )
        //            )
        //            .flatMap($ -> scanForViruses(content))
        //            .mapLeft(ex ->
        //                new ValidationError(
        //                    "failed to check document for virus: " + ex.msg()
        //                )
        //            )
        //            .map(hasVirus ->
        //                new ValidationStatus(
        //                    hasVirus,
        //                    "successfully validated document",
        //                    "ClamAV"
        //                )
        //            );
    }

    //    private Either<ValidationError, Boolean> scanForViruses(byte[] content) {
    //        return virusScanner
    //            .scan(content)
    //            .mapLeft(scanError ->
    //                new ValidationError(
    //                    "failed to check virus in document: " + scanError
    //                )
    //            );
    //    }
    //
    //    private Either<ValidationError, PDDocument> validateStructure(
    //        byte[] content
    //    ) {
    //        return loadPDF(content)
    //            .flatMap(this::validateNotEncrypted)
    //            .flatMap(this::validateNotEmpty);
    //    }
    //
    //    private Either<ValidationError, PDDocument> loadPDF(byte[] content) {
    //        return Try.withResources(() -> Loader.loadPDF(content))
    //            .of(pdDocument -> pdDocument)
    //            .toEither()
    //            .mapLeft(ex ->
    //                new ValidationError(
    //                    "failed to load document: " + ex.getMessage()
    //                )
    //            );
    //    }
    //
    //    private Either<ValidationError, PDDocument> validateNotEmpty(
    //        PDDocument pdDocument
    //    ) {
    //        return pdDocument.getNumberOfPages() == 0
    //            ? Either.left(new ValidationError("document is empty"))
    //            : Either.right(pdDocument);
    //    }
    //
    //    private Either<ValidationError, PDDocument> validateNotEncrypted(
    //        PDDocument pdDocument
    //    ) {
    //        return pdDocument.isEncrypted()
    //            ? Either.left(new ValidationError("document is encrypted"))
    //            : Either.right(pdDocument);
    //    }
}
