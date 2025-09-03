package com.mochaeng.theia_api.processing.infrastructure.adapter.ollama.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OllamaResponse(
    String model,
    Float[][] embeddings,

    @JsonProperty("total_duration") long totalDuration,

    @JsonProperty("load_duration") long loadDuration,

    @JsonProperty("prompt_eval_count") int promptEvalCount
) {
    public boolean hasEmbeddings() {
        return (
            embeddings != null &&
            embeddings.length > 0 &&
            embeddings[0].length > 0
        );
    }

    public Float[] getFirstEmbedding() {
        return hasEmbeddings() ? embeddings[0] : null;
    }

    public long getProcessingTimeMs() {
        return totalDuration / 1_000_000;
    }
}
