package com.mochaeng.theia_api.query.application.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record QueryResponse(
    List<DocumentResult> results,

    @JsonProperty("totalResults") Integer totalResults,

    @JsonProperty("queryTime") String queryTime
) {}
