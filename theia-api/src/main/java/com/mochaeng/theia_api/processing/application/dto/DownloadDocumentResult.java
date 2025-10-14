package com.mochaeng.theia_api.processing.application.dto;

import java.util.Arrays;
import lombok.Builder;

@Builder
public record DownloadDocumentResult(byte[] content, byte[] hash) {
    public DownloadDocumentResult {
        if (content != null) {
            content = Arrays.copyOf(content, content.length);
        }

        if (hash != null) {
            hash = Arrays.copyOf(hash, hash.length);
        }
    }

    @Override
    public byte[] content() {
        if (content == null) {
            return null;
        }
        return Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] hash() {
        if (hash == null) {
            return null;
        }
        return Arrays.copyOf(hash, hash.length);
    }
}
