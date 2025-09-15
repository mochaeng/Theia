package com.mochaeng.theia_api.query.application.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorResult(
    @JsonProperty("firstName") String firstName,

    @JsonProperty("lastName") String lastName,

    String email
) {}
