package com.mochaeng.theia_api.document.dto;

public record UploadDocumentResponse(
    String documentID,
    String originalFileName
) {
}
