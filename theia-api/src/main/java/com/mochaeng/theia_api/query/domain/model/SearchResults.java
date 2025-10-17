package com.mochaeng.theia_api.query.domain.model;

import java.util.List;
import lombok.Builder;

@Builder
public record SearchResults(
    List<DocumentSearch> results,
    Integer totalResults,
    Long queryTimeMs
) {}
