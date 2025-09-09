package com.mochaeng.theia_api.ingestion.application.service;

import com.mochaeng.theia_api.ingestion.application.port.in.ValidateDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.out.ScanVirusPort;
import com.mochaeng.theia_api.ingestion.domain.exceptions.DocumentValidationErrorCode;
import com.mochaeng.theia_api.ingestion.domain.exceptions.DocumentValidationException;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.processing.infrastructure.adapter.persistence.DocumentPersistenceService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ValidateDocumentService implements ValidateDocumentUseCase {

    private final long maxFileSizeBytes;
    private final List<String> allowedContentTypes;
    private final ScanVirusPort virusScanService;
    private final DocumentPersistenceService documentPersistenceService;

    public ValidateDocumentService(
        @Value("${app.upload.max-file-size:10485760}") long maxFileSizeBytes,
        @Value("${app.upload.allowed-content-types:application/pdf}") List<
            String
        > allowedContentTypes,
        ScanVirusPort virusScanService,
        DocumentPersistenceService documentPersistenceService
    ) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedContentTypes = List.copyOf(allowedContentTypes);
        this.virusScanService = virusScanService;
        this.documentPersistenceService = documentPersistenceService;
    }

    @Override
    public void validate(Document document) {
        log.debug("Validating document: {}", document.filename());

        validateDocumentExistence(document);
        validateFileSize(document);
        validatePdfMagicBytes(document);
        validateStructure(document);
        validateFileType(document);
        validateVirusFree(document);

        log.debug(
            "Document validation completed successfully for: {}",
            document.filename()
        );
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
        if (content == null) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.INVALID_PDF
            );
        }
        if (
            content.length < 5 ||
            !(new String(content, 0, 5, StandardCharsets.UTF_8).startsWith(
                    "%PDF-"
                ))
        ) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.INVALID_PDF
            );
        }
    }

    private void validateStructure(Document document) {
        try (
            var inputStream = new ByteArrayInputStream(
                Objects.requireNonNull(document.content())
            )
        ) {
            validateBasicStructure(inputStream);
        } catch (InvalidPasswordException e) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.DOCUMENT_ENCRYPTED
            );
        } catch (IOException e) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.DOCUMENT_CORRUPTED
            );
        }
    }

    private void validateBasicStructure(ByteArrayInputStream inputStream)
        throws IOException {
        try (var doc = Loader.loadPDF(inputStream.readAllBytes())) {
            if (doc.getNumberOfPages() == 0) {
                throw new DocumentValidationException(
                    DocumentValidationErrorCode.DOCUMENT_EMPTY
                );
            }

            if (doc.isEncrypted()) {
                throw new DocumentValidationException(
                    DocumentValidationErrorCode.DOCUMENT_ENCRYPTED
                );
            }
        }
    }

    private void validateFileType(Document document) {
        if (
            document.contentType() == null ||
            !allowedContentTypes.contains(document.contentType())
        ) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.INVALID_FILE_TYPE,
                String.format(
                    "File type '%s' is not allowed. Allowed types: %s",
                    document.contentType(),
                    allowedContentTypes
                )
            );
        }
    }

    private void validateVirusFree(Document document) {
        if (virusScanService.hasVirus(document)) {
            throw new DocumentValidationException(
                DocumentValidationErrorCode.VIRUS_DETECTED
            );
        }
    }
}
