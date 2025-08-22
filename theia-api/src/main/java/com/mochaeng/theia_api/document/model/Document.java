package com.mochaeng.theia_api.document.model;

public record Document(
    String filename,
    String contentType,
    byte[] content) {
}
