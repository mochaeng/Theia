package com.mochaeng.theia_api.ingestion.application.service;

import com.mochaeng.theia_api.ingestion.application.port.in.AcceptDocumentUseCase;
import com.mochaeng.theia_api.ingestion.domain.exceptions.DocumentValidationErrorCode;
import com.mochaeng.theia_api.ingestion.domain.exceptions.DocumentValidationException;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.processing.infrastructure.adapter.persistence.DocumentPersistenceService;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AcceptDocumentService implements AcceptDocumentUseCase {

    private final long maxFileSizeBytes;
    private final DocumentPersistenceService documentPersistenceService;

    public AcceptDocumentService(
        @Value("${app.upload.max-file-size:10485760}") long maxFileSizeBytes,
        DocumentPersistenceService documentPersistenceService
    ) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.documentPersistenceService = documentPersistenceService;
    }

    @Override
    public Either<AcceptDocumentError, Void> validate(Document document) {
        log.info("starting basic document validation for '{}'", document.id());

        return Try.run(() -> {
            validateDocumentExistence(document);
            validateFileSize(document);
            validatePdfMagicBytes(document);
        })
            .toEither()
            .mapLeft(ex -> new AcceptDocumentError(ex.getMessage()));
    }

    private void validateDocumentExistence(Document document) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(document.content());
            if (documentPersistenceService.existsByHash(hash)) {
                throw new DocumentValidationException(
                    DocumentValidationErrorCode.DOCUMENT_ALREADY_PROCESSED
                );
            }
        } catch (NoSuchAlgorithmException e) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.DOCUMENT_HASH_FAILED,
                "No instance fr"
            );
        }
    }

    private void validateFileSize(Document document) {
        if (
            Objects.requireNonNull(document.content()).length > maxFileSizeBytes
        ) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.FILE_TOO_LARGE,
                String.format(
                    "File size %d bytes exceeds maximum allowed size of %d bytes",
                    Objects.requireNonNull(document.content()).length,
                    maxFileSizeBytes
                )
            );
        }
    }

    private void validatePdfMagicBytes(Document document) {
        var content = document.content();
        if (content == null || content.length < 5) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.INVALID_PDF
            );
        }

        var firstBytes = new String(content, 0, 5, StandardCharsets.UTF_8);
        if (!(firstBytes.startsWith("%PDF-"))) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.INVALID_PDF
            );
        }
    }

    //    private void validateVirusFree(Document document) {
    //        if (virusScanService.hasVirus(document)) {
    //            throw new DocumentValidationException(
    //                DocumentValidationErrorCode.VIRUS_DETECTED
    //            );
    //        }
    //    }
}
