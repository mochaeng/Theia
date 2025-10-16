package com.mochaeng.theia_api.processing.domain.model;

import lombok.Builder;

@Builder
public record Author(String firstName, String lastName, String email) {
    public Author {
        firstName = firstName.isEmpty() ? null : firstName;
        lastName = lastName.isEmpty() ? null : lastName;
        email = email.isEmpty() ? null : email;
    }
}
