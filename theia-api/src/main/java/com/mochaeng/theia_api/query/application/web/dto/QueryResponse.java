package com.mochaeng.theia_api.query.application.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mochaeng.theia_api.query.domain.model.DocumentSearch;
import com.mochaeng.theia_api.query.domain.model.SearchResults;
import java.util.List;

public record QueryResponse(
    List<DocumentSearch> results,
    @JsonProperty("totalResults") Integer totalResults,
    @JsonProperty("queryTime") Long queryTime
) {
    public QueryResponse {
        results = results != null ? List.copyOf(results) : List.of();
    }

    public static QueryResponse from(SearchResults search) {
        return new QueryResponse(
            search.results(),
            search.totalResults(),
            search.queryTimeMs()
        );
    }
}
