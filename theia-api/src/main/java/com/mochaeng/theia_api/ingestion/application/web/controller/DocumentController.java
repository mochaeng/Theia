package com.mochaeng.theia_api.ingestion.application.web.controller;

import com.mochaeng.theia_api.ingestion.application.port.in.UploadDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.web.dto.UploadDocumentResponse;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;

    @PostMapping(
        value = "/upload-document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UploadDocumentResponse> uploadDocument(
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info(
            "Receive upload request for file: {} (size: {} bytes)",
            file.getOriginalFilename(),
            file.getSize()
        );

        Document document = Document.create(
            file.getContentType(),
            file.getBytes()
        );

        uploadDocumentUseCase.uploadDocument(document);

        return ResponseEntity.ok(
            new UploadDocumentResponse(document.id().toString())
        );
    }
}
