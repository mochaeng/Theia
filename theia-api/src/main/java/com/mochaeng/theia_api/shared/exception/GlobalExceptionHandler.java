package com.mochaeng.theia_api.shared.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mochaeng.theia_api.ingestion.domain.exceptions.DocumentValidationException;
import com.mochaeng.theia_api.processing.application.exceptions.DownloadDocumentException;
import com.mochaeng.theia_api.shared.dto.ErrorResponse;
import java.security.UnrecoverableKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        log.error(
            "validation error: {} - {}",
            ex.getErrorCode(),
            ex.getMessage()
        );

        var errorResponse = new ErrorResponse(
            ex.getErrorCodeValue(),
            "Failed to validate your document.",
            extractPath(request)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
        MaxUploadSizeExceededException ex,
        WebRequest request
    ) {
        log.warn("Upload size exceeded: {}", ex.getMessage());

        var errorResponse = new ErrorResponse(
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

        var errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            "Invalid input provided",
            extractPath(request)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        WebRequest request
    ) {
        if (ex.getCause() instanceof UnrecognizedPropertyException upex) {
            log.error("Unrecognized property: {}", upex.getPropertyName());

            var errorResponse = new ErrorResponse(
                "REQUEST_VALIDATION_ERROR",
                "Request contains unrecognized field: " +
                upex.getPropertyName(),
                extractPath(request)
            );

            return ResponseEntity.badRequest().body(errorResponse);
        }

        log.error("Malformed JSON request: {}", ex.getMessage());
        var errorResponse = new ErrorResponse(
            "MALFORMED_JSON",
            "Request body is invalid or malformed.",
            extractPath(request)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        var errors = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .toList();

        log.error("validation failed: {}", errors);

        var errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            String.join("; ", errors),
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

        var errorResponse = new ErrorResponse(
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
