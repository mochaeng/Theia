package com.mochaeng.theia_api.document.service;

import com.mochaeng.theia_api.shared.exception.ValidationException;
import com.mochaeng.theia_api.document.model.Document;
import com.mochaeng.theia_api.document.validation.DocumentValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DocumentValidationServiceImpl implements DocumentValidationService {
    private final long maxFileSizeBytes;
    private final List<String> allowedContentTypes;
    private final VirusScanService virusScanService;

    public DocumentValidationServiceImpl(
        @Value("${app.upload.max-file-size:10485760}") long maxFileSizeBytes,
        @Value("${app.upload.allowed-content-types:application/pdf}") List<String> allowedContentTypes,
        VirusScanService virusScanService
    ) {
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedContentTypes = allowedContentTypes;
        this.virusScanService = virusScanService;
    }

    @Override
    public void validateDocument(Document document) {
        log.debug("Validating document: {}", document.filename());

        validateFileType(document);
        validateFileSize(document);
        validateVirusFree(document);

        log.debug("Document validation completed successfully for: {}", document.filename());
    }

    private void validateFileType(Document document) {
        if (
            document.contentType() == null ||
                !allowedContentTypes.contains(document.contentType())
        ) {
            throw new ValidationException(
                "INVALID_FILE_TYPE",
                String.format("File type '%s' is not allowed. Allowed types: %s",
                    document.contentType(),
                    allowedContentTypes
                )
            );
        }
    }

    private void validateFileSize(Document document) {
        if (document.content().length > maxFileSizeBytes) {
            throw new ValidationException(
                "FILE_TOO_LARGE",
                String.format("File size %d bytes exceeds maximum allowed size of %d bytes",
                    document.content().length,
                    maxFileSizeBytes
                )
            );
        }
    }

    private void validateVirusFree(Document document) {
        if (virusScanService.hasVirus(document)) {
            throw new ValidationException(
                "VIRUS_DETECTED",
                "File contains malicious content and cannot be processed"
            );
        }
    }
}
