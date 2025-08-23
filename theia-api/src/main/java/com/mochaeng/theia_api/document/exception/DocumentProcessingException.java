package com.mochaeng.theia_api.document.exception;

import lombok.Getter;

@Getter
public class DocumentProcessingException extends RuntimeException {
  private final String errorCode;

  public DocumentProcessingException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public DocumentProcessingException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
