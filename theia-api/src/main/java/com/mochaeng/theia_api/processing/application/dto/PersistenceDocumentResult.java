package com.mochaeng.theia_api.processing.application.dto;

import com.mochaeng.theia_api.processing.domain.model.DocumentEmbeddings;
import lombok.Builder;

@Builder
public record PersistenceDocumentResult(
    boolean isSuccess,
    String errorCode,
    String errorMessage
) {
    public static PersistenceDocumentResult success() {
        return PersistenceDocumentResult.builder().isSuccess(true).build();
    }

    public static PersistenceDocumentResult failure(
        String errorCode,
        String errorMessage
    ) {
        return PersistenceDocumentResult.builder()
            .isSuccess(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
