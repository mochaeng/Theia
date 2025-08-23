package com.mochaeng.theia_api.document.service;

import com.mochaeng.theia_api.document.exception.DocumentValidationErrorCode;
import com.mochaeng.theia_api.document.exception.DocumentValidationException;
import com.mochaeng.theia_api.document.model.Document;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentValidationServiceImpl implements DocumentValidationService {
  private final long maxFileSizeBytes;
  private final List<String> allowedContentTypes;
  private final VirusScanService virusScanService;

  public DocumentValidationServiceImpl(
      @Value("${app.upload.max-file-size:10485760}") long maxFileSizeBytes,
      @Value("${app.upload.allowed-content-types:application/pdf}")
          List<String> allowedContentTypes,
      VirusScanService virusScanService) {
    this.maxFileSizeBytes = maxFileSizeBytes;
    this.allowedContentTypes = List.copyOf(allowedContentTypes);
    this.virusScanService = virusScanService;
  }

  @Override
  public void validateDocument(Document document) {
    log.debug("Validating document: {}", document.filename());

    validateFileSize(document);
    validatePdfMagicBytes(document);
    validateStructure(document);
    validateFileType(document);
    validateVirusFree(document);

    log.debug("Document validation completed successfully for: {}", document.filename());
  }

  private void validatePdfMagicBytes(Document document) {
    byte[] content = document.content();
    if (content == null) {
      throw new DocumentValidationException(DocumentValidationErrorCode.INVALID_PDF);
    }
    if (content.length < 5
        || !(new String(content, 0, 5, StandardCharsets.UTF_8).startsWith("%PDF-"))) {
      throw new DocumentValidationException(DocumentValidationErrorCode.INVALID_PDF);
    }
  }

  private void validateStructure(Document document) {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(document.content())) {
      validateBasicStructure(inputStream);
    } catch (InvalidPasswordException e) {
      throw new DocumentValidationException(DocumentValidationErrorCode.PDF_ENCRYPTED);
    } catch (IOException e) {
      throw new DocumentValidationException(DocumentValidationErrorCode.PDF_CORRUPTED);
    }
  }

  private void validateBasicStructure(ByteArrayInputStream inputStream) throws IOException {
    try (PDDocument doc = Loader.loadPDF(inputStream.readAllBytes())) {
      if (doc.getNumberOfPages() == 0) {
        throw new DocumentValidationException(DocumentValidationErrorCode.PDF_EMPTY);
      }

      if (doc.isEncrypted()) {
        throw new DocumentValidationException(DocumentValidationErrorCode.PDF_ENCRYPTED);
      }
    }
  }

  private void validateFileType(Document document) {
    if (document.contentType() == null || !allowedContentTypes.contains(document.contentType())) {
      throw new DocumentValidationException(
          DocumentValidationErrorCode.INVALID_FILE_TYPE,
          String.format(
              "File type '%s' is not allowed. Allowed types: %s",
              document.contentType(), allowedContentTypes));
    }
  }

  private void validateFileSize(Document document) {
    if (document.content().length > maxFileSizeBytes) {
      throw new DocumentValidationException(
          DocumentValidationErrorCode.FILE_TOO_LARGE,
          String.format(
              "File size %d bytes exceeds maximum allowed size of %d bytes",
              document.content().length, maxFileSizeBytes));
    }
  }

  private void validateVirusFree(Document document) {
    if (virusScanService.hasVirus(document)) {
      throw new DocumentValidationException(DocumentValidationErrorCode.VIRUS_DETECTED);
    }
  }
}
