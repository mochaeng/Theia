package com.mochaeng.theia_api.ingestion.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mochaeng.theia_api.ingestion.application.port.in.AcceptDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.in.UploadIncomingDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.out.FileStoragePort;
import com.mochaeng.theia_api.ingestion.application.port.out.PublishIncomingDocumentPort;
import com.mochaeng.theia_api.ingestion.application.service.AcceptDocumentService;
import com.mochaeng.theia_api.ingestion.application.service.UploadIncomingDocumentService;
import com.mochaeng.theia_api.ingestion.application.web.UploadController;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.processing.infrastructure.adapter.persistence.DocumentPersistenceService;
import io.vavr.control.Either;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;

@WebMvcTest(UploadController.class)
@ActiveProfiles("test")
public class DocumentUploadControllerTest {

    private static final String UPLOAD_ENDPOINT = "/api/upload-document";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        MediaType.APPLICATION_PDF_VALUE
    );

    private static final String BITCOIN_PDF_PATH = "test-files/bitcoin.pdf";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStoragePort storageService;

    @MockitoBean
    private DocumentPersistenceService documentPersistenceService;

    @MockitoBean
    private PublishIncomingDocumentPort kafkaEventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName(
        "Should successfully upload Bitcoin whitepaper PDF and return valid UUID"
    )
    void uploadDocument_WithValidPDF_ShouldReturnUUID() throws Exception {
        when(storageService.storeDocument(any(), any(), any())).thenReturn(
            Either.right("mocked-s3-path")
        );
        doNothing().when(kafkaEventPublisher).publishAsync(any());

        MockMultipartFile bitcoinPdf = createRealPdfFile(
            BITCOIN_PDF_PATH,
            "bitcoin.pdf"
        );

        assertThat(bitcoinPdf.getSize()).isGreaterThan(0);
        assertThat(bitcoinPdf.getOriginalFilename()).isEqualTo("bitcoin.pdf");
        assertThat(bitcoinPdf.getContentType()).isEqualTo(
            MediaType.APPLICATION_PDF_VALUE
        );

        MvcResult result = mockMvc
            .perform(multipart(UPLOAD_ENDPOINT).file(bitcoinPdf))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.documentID").exists())
            .andReturn();

        String documentId = extractDocumentIdFromResponse(result);
        assertValidUUID(documentId);

        verify(storageService, times(1)).storeDocument(any(), any(), any());
        verify(kafkaEventPublisher, times(1)).publishAsync(any());

        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(
            Document.class
        );
        verify(storageService).storeDocument(
            any(),
            any(),
            documentCaptor.capture()
        );
        assertThat(documentCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("Should reject text file with unsupported content type")
    void uploadDocument_WithTextFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile textFile = createMockFile(
            "document.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Sample text".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc
            .perform(multipart(UPLOAD_ENDPOINT).file(textFile))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verifyNoInteractions(storageService, kafkaEventPublisher);
    }

    @Test
    @DisplayName("Should reject PDF file that exceeds size limit")
    void uploadDocument_WithOversizedPDF_ShouldReturnBadRequest()
        throws Exception {
        byte[] oversizedContent = new byte[(int) MAX_FILE_SIZE + 1];
        MockMultipartFile oversizedFile = createMockFile(
            "large.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            oversizedContent
        );

        mockMvc
            .perform(multipart(UPLOAD_ENDPOINT).file(oversizedFile))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verifyNoInteractions(storageService, kafkaEventPublisher);
    }

    @Test
    @DisplayName("Should reject empty PDF file")
    void uploadDocument_WithEmptyPDF_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile emptyFile = createMockFile(
            "empty.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            new byte[0]
        );

        mockMvc
            .perform(multipart(UPLOAD_ENDPOINT).file(emptyFile))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verifyNoInteractions(storageService, kafkaEventPublisher);
    }

    @Test
    @DisplayName("Should reject PDF file containing virus")
    void uploadDocument_WithVirusInfectedPDF_ShouldReturnBadRequest()
        throws Exception {
        MockMultipartFile infectedFile;

        infectedFile = createMockFile(
            "infected.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            createMinimalPdfContent()
        );

        mockMvc
            .perform(multipart(UPLOAD_ENDPOINT).file(infectedFile))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verifyNoInteractions(storageService, kafkaEventPublisher);
    }

    private MockMultipartFile createMockFile(
        String filename,
        String contentType,
        byte[] content
    ) {
        return new MockMultipartFile("file", filename, contentType, content);
    }

    private void assertValidUUID(String uuidString) {
        UUID uuid = assertDoesNotThrow(() -> UUID.fromString(uuidString));
        assertThat(uuid.toString()).isEqualTo(uuidString);
    }

    private String extractDocumentIdFromResponse(MvcResult result)
        throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        return jsonResponse.get("documentID").asText();
    }

    private MockMultipartFile createRealPdfFile(
        String resourcePath,
        String filename
    ) throws IOException {
        byte[] pdfContent = loadPdfFromResources(resourcePath);
        return new MockMultipartFile(
            "file",
            filename,
            MediaType.APPLICATION_PDF_VALUE,
            pdfContent
        );
    }

    private byte[] loadPdfFromResources(String resourcePath)
        throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToByteArray(inputStream);
        }
    }

    private byte[] createMinimalPdfContent() {
        return "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f \n0000000010 00000 n \n0000000079 00000 n \n0000000173 00000 n \ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n253\n%%EOF".getBytes(
            StandardCharsets.UTF_8
        );
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public AcceptDocumentUseCase documentValidationService(
            DocumentPersistenceService documentPersistenceService
        ) {
            return new AcceptDocumentService(
                MAX_FILE_SIZE,
                documentPersistenceService
            );
        }

        @Bean
        @Primary
        public UploadIncomingDocumentUseCase documentService(
            AcceptDocumentUseCase documentValidationService,
            FileStoragePort storageService,
            PublishIncomingDocumentPort kafkaEventPublisher
        ) {
            return new UploadIncomingDocumentService(
                documentValidationService,
                storageService,
                kafkaEventPublisher
            );
        }
    }
}
