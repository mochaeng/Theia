package com.mochaeng.theia_api.ingestion.domain.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentValidationErrorCode {
    INVALID_PDF("INVALID_PDF", "File is not a valid PDF format"),
    PDF_ENCRYPTED(
        "PDF_ENCRYPTED",
        "PDF is password-protected and cannot be processed"
    ),
    PDF_CORRUPTED(
        "PDF_CORRUPTED",
        "PDF file is corrupted or has invalid structure"
    ),
    PDF_EMPTY("PDF_EMPTY", "PDF contains no pages"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "File type is not allowed"),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "File size exceeds maximum allowed size"),
    VIRUS_DETECTED(
        "VIRUS_DETECTED",
        "File contains malicious content and cannot be processed"
    );

    private final String code;
    private final String defaultMessage;

    public String formatMessage(Object... args) {
        return String.format(defaultMessage, args);
    }
}
