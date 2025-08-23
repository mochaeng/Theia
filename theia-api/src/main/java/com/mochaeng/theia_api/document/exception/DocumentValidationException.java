package com.mochaeng.theia_api.document.exception;

import lombok.Getter;

@Getter
public class DocumentValidationException extends RuntimeException {
  private final DocumentValidationErrorCode errorCode;

  public DocumentValidationException(DocumentValidationErrorCode errorCode) {
    super(errorCode.getDefaultMessage());
    this.errorCode = errorCode;
  }

  public DocumentValidationException(DocumentValidationErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
  }

  public DocumentValidationException(DocumentValidationErrorCode errorCode, Object... messageArgs) {
    super(errorCode.formatMessage(messageArgs));
    this.errorCode = errorCode;
  }

  public DocumentValidationException(DocumentValidationErrorCode errorCode, Throwable cause) {
    super(errorCode.getDefaultMessage(), cause);
    this.errorCode = errorCode;
  }

  public String getErrorCodeValue() {
    return errorCode.getCode();
  }
}
