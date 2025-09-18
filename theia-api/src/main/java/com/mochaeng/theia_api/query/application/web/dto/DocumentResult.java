package com.mochaeng.theia_api.query.application.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentResult(
    @JsonProperty("documentID") UUID documentID,

    String title,
    Float similarity,
    List<AuthorResult> authors,

    @JsonProperty("createdAt") LocalDateTime createdAt,

    @JsonProperty("updatedAt") LocalDateTime updatedAt
) {}
