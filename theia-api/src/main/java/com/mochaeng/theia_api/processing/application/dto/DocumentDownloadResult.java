package com.mochaeng.theia_api.processing.application.dto;

import java.util.Arrays;
import lombok.Builder;

@Builder
public record DocumentDownloadResult(
    byte[] content,
    ErrorCode errorCode,
    String errorMessage
) {
    public enum ErrorCode {
        EMPTY_DOCUMENT,
        INVALID_FILE_SIZE,
        DOCUMENT_NOT_FOUND,
        SPECIFIC_ERROR,
        UNEXPECTED_ERROR,
    }

    public DocumentDownloadResult {
        if (content != null) {
            content = Arrays.copyOf(content, content.length);
        }
    }

    @Override
    public byte[] content() {
        if (content == null) {
            return null;
        }
        return Arrays.copyOf(content, content.length);
    }

    public static DocumentDownloadResult success(byte[] content) {
        return DocumentDownloadResult.builder().content(content).build();
    }

    public static DocumentDownloadResult failure(
        ErrorCode errorCode,
        String errorMessage
    ) {
        return DocumentDownloadResult.builder()
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }

    public boolean isSuccess() {
        return errorCode == null;
    }
}
