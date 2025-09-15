package com.mochaeng.theia_api.query.domain.model;

import java.util.List;

public record Search(
    List<DocumentSearch> results,
    Integer totalResults,
    Long queryTimeMs
) {}
