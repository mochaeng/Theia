package com.mochaeng.theia_api.ingestion.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentValidationErrorCode {
    INVALID_PDF("INVALID_PDF", "File is not a valid PDF format"),
    DOCUMENT_ENCRYPTED(
        "PDF_ENCRYPTED",
        "PDF is password-protected and cannot be processed"
    ),
    DOCUMENT_CORRUPTED(
        "PDF_CORRUPTED",
        "PDF file is corrupted or has invalid structure"
    ),
    DOCUMENT_EMPTY("PDF_EMPTY", "PDF contains no pages"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "File type is not allowed"),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "File size exceeds maximum allowed size"),
    VIRUS_DETECTED(
        "VIRUS_DETECTED",
        "File contains malicious content and cannot be processed"
    ),
    DOCUMENT_ALREADY_PROCESSED(
        "DOCUMENT_ALREADY_PROCESSED",
        "Document was already processed"
    ),
    DOCUMENT_HASH_FAILED("DOCUMENT_HASH_FAILED", "Failed to hash document");

    private final String code;
    private final String defaultMessage;

    public String formatMessage(Object... args) {
        return String.format(defaultMessage, args);
    }
}
