package com.mochaeng.theia_api.shared.exception;

import com.mochaeng.theia_api.ingestion.domain.exceptions.DocumentValidationException;
import com.mochaeng.theia_api.shared.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        DocumentValidationException ex,
        WebRequest request
    ) {
        log.warn(
            "Validation error: {} - {}",
            ex.getErrorCode(),
            ex.getMessage()
        );

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCodeValue(),
            ex.getMessage(),
            extractPath(request)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    //    @ExceptionHandler(DocumentProcessingException.class)
    //    public ResponseEntity<ErrorResponse> handleDocumentProcessingException(
    //        DocumentProcessingException ex,
    //        WebRequest request
    //    ) {
    //        log.error(
    //            "Document processing error: {} - {}",
    //            ex.getErrorCode(),
    //            ex.getMessage(),
    //            ex
    //        );
    //
    //        ErrorResponse errorResponse = new ErrorResponse(
    //            ex.getErrorCode(),
    //            ex.getMessage(),
    //            extractPath(request)
    //        );
    //
    //        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
    //            errorResponse
    //        );
    //    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
        MaxUploadSizeExceededException ex,
        WebRequest request
    ) {
        log.warn("Upload size exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "FILE_SIZE_EXCEEDED",
            "File size exceeds the maximum allowed limit",
            extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
            errorResponse
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
        IllegalArgumentException ex,
        WebRequest request
    ) {
        log.warn("Invalid argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            "Invalid input provided",
            extractPath(request)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request
    ) {
        log.error(
            "Unexpected error occurred: {} - {}",
            ex.getClass().getSimpleName(),
            ex.getMessage(),
            ex
        );

        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred. Please try again later.",
            extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            errorResponse
        );
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
