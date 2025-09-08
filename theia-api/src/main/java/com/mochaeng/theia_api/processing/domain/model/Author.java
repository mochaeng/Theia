package com.mochaeng.theia_api.processing.domain.model;

import lombok.Builder;

@Builder
public record Author(String firstName, String lastName, String email) {}
