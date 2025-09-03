package com.mochaeng.theia_api.processing.application.dto;

import lombok.Builder;

@Builder
public record EmbeddingDocumentResult(
    boolean isSuccess,
    DocumentEmbedding embedding,
    String errorCode,
    String errorMessage
) {
    public static EmbeddingDocumentResult success(DocumentEmbedding embedding) {
        return EmbeddingDocumentResult.builder()
            .isSuccess(true)
            .embedding(embedding)
            .build();
    }

    public static EmbeddingDocumentResult failure(
        String errorCode,
        String errorMessage
    ) {
        return EmbeddingDocumentResult.builder()
            .isSuccess(false)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .build();
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
