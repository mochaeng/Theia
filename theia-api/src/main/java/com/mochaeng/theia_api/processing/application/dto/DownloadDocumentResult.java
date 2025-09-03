package com.mochaeng.theia_api.processing.application.dto;

import java.util.Arrays;
import lombok.Builder;

@Builder
public record DownloadDocumentResult(
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

    public DownloadDocumentResult {
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

    public static DownloadDocumentResult success(byte[] content) {
        return DownloadDocumentResult.builder().content(content).build();
    }

    public static DownloadDocumentResult failure(
        ErrorCode errorCode,
        String errorMessage
    ) {
        return DownloadDocumentResult.builder()
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }

    public boolean isSuccess() {
        return errorCode == null;
    }
}
