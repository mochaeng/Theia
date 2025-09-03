package com.mochaeng.theia_api.processing.application.dto;

import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import lombok.Builder;

@Builder
public record ExtractDocumentResult(
    boolean isSuccess,
    DocumentMetadata metadata,
    String errorCode,
    String errorMessage
) {
    public static ExtractDocumentResult success(DocumentMetadata metadata) {
        return ExtractDocumentResult.builder()
            .isSuccess(true)
            .metadata(metadata)
            .build();
    }

    public static ExtractDocumentResult failure(
        String errorCode,
        String errorMessage
    ) {
        return ExtractDocumentResult.builder()
            .isSuccess(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }
}
