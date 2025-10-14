package com.mochaeng.theia_api.ingestion.application.web;

import com.mochaeng.theia_api.ingestion.application.port.in.UploadIncomingDocumentUseCase;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.shared.dto.ErrorResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final UploadIncomingDocumentUseCase uploadDocumentUseCase;

    @PreAuthorize("hasRole('uploader')")
    @PostMapping(
        value = "/upload-document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        log.info(
            "receive upload request for file '{}' with size {} bytes)",
            file.getOriginalFilename(),
            file.getSize()
        );

        var userID = jwt.getSubject();

        var document = Document.create(
            file.getContentType(),
            userID,
            file.getBytes()
        );
        if (document.isLeft()) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse(document.getLeft(), "/v1/upload-document")
            );
        }

        return uploadDocumentUseCase
            .upload(document.get())
            .fold(this::mapError, this::mapResponse);
    }

    private ResponseEntity<UploadDocumentResponse> mapResponse(
        DocumentMessage msg
    ) {
        return ResponseEntity.ok(
            new UploadDocumentResponse(msg.documentID().toString())
        );
    }

    private ResponseEntity<ErrorResponse> mapError(
        UploadIncomingDocumentUseCase.UploadError error
    ) {
        return ResponseEntity.badRequest().body(
            new ErrorResponse("Invalid file type", "/v1/upload-document")
        );
    }
}
