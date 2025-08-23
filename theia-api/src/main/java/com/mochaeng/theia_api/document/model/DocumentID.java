package com.mochaeng.theia_api.document.model;

import java.util.UUID;

public record DocumentID(String value) {
  public DocumentID {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("value cannot be null or empty");
    }
  }

  public static DocumentID generate() {
    return new DocumentID(UUID.randomUUID().toString());
  }

  public static DocumentID fromString(String value) {
    return new DocumentID(value);
  }
}
