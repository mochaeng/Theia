package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mochaeng.theia_api.processing.application.dto.ExtractDocumentResult;
import com.mochaeng.theia_api.processing.application.port.out.ExtractDocumentDataPort;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidParsingException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidTimeoutException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidUnavailableException;
import com.mochaeng.theia_api.shared.application.dto.DocumentUploadedMessage;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component("grobidExtractDocumentData")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class GrobidExtractDocumentData implements ExtractDocumentDataPort {

    @Qualifier("grobidRestClient")
    private final RestClient restClient;

    @Qualifier("grobidRetryTemplate")
    private final RetryTemplate retryTemplate;

    @Qualifier("grobidXmlMapper")
    private final XmlMapper xmlMapper;

    private final GrobidProperties grobidProperties;

    @Override
    public ExtractDocumentResult extract(
        DocumentUploadedMessage message,
        byte[] documentBytes
    ) {
        log.info(
            "Starting Grobid extraction for document: {}",
            message.documentID()
        );

        try {
            String teiXml = callGrobidWithRetry(message, documentBytes);
            var grobidData = parseTeiResponse(teiXml);
            var metadata = createMetadata(message.documentID(), grobidData);

            log.info(
                "Successfully extracted metadata for document: {}",
                message.documentID()
            );
            return ExtractDocumentResult.success(metadata);
        } catch (GrobidUnavailableException e) {
            log.error("Grobid is unavailable", e);
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                "Service unreachable"
            );
        } catch (GrobidTimeoutException e) {
            log.error("Timeout calling Grobid for document {}", e.getMessage());
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                "Request to Grobid timed out"
            );
        } catch (GrobidParsingException e) {
            log.error("Grobid parsing failure", e);
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                "Grobid parsing failure"
            );
        } catch (GrobidException e) {
            log.error(
                "Grobid extraction failed for document: {}",
                message.documentID(),
                e
            );
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                e.getMessage()
            );
        } catch (Exception e) {
            log.error(
                "Unexpected error extraction for document: {}",
                message.documentID(),
                e
            );
            return ExtractDocumentResult.failure(
                "UNEXPECTED_ERROR",
                e.getMessage()
            );
        }
    }

    private String callGrobidWithRetry(
        DocumentUploadedMessage message,
        byte[] documentBytes
    ) {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                log.info(
                    "Retrying Grobid call for document: {} (attempt {})",
                    message.documentID(),
                    context.getRetryCount() + 1
                );
            }

            try {
                return makeGrobidCall(message, documentBytes);
            } catch (ResourceAccessException e) {
                log.warn(
                    "Network error calling Grobid for document {}, attempt {}: {}",
                    message.documentID(),
                    context.getRetryCount(),
                    e.getMessage()
                );
                throw e;
            }
        });
    }

    private String makeGrobidCall(
        DocumentUploadedMessage message,
        byte[] documentBytes
    ) {
        log.debug(
            "Making HTTP call to Grobid for document: {}",
            message.documentID()
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder
            .part("input", documentBytes)
            .filename("document.pdf")
            .contentType(MediaType.APPLICATION_PDF);
        builder.part(
            "consolidateHeader",
            grobidProperties.getConsolidateHeader().getValue()
        );

        try {
            return restClient
                .post()
                .uri("/api/processHeaderDocument")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Accept", MediaType.APPLICATION_XML_VALUE)
                .body(builder.build())
                .retrieve()
                .body(String.class);
        } catch (ResourceAccessException e) {
            log.debug(
                "ResourceAccessException for Grobid details - message: {}, cause: {}, cause type: {}",
                e.getMessage(),
                e.getCause(),
                e.getCause() != null
                    ? e.getCause().getClass().getName()
                    : "null"
            );

            Throwable cause = e.getCause();

            if (cause instanceof SocketTimeoutException) {
                throw new GrobidTimeoutException(
                    "Timeout while calling Grobid",
                    cause
                );
            }

            if (
                cause instanceof java.net.ConnectException ||
                cause instanceof java.net.UnknownHostException
            ) {
                throw new GrobidUnavailableException(
                    "Grobid service is unreachable",
                    cause
                );
            }

            throw e;
        }
    }

    private GrobidData parseTeiResponse(String teiXml) {
        try {
            log.debug("Parsing TEI XML response: {}", teiXml);

            return xmlMapper.readValue(teiXml, GrobidData.class);
        } catch (Exception e) {
            log.error("Failed to parse Grobid TEI XML response", e);
            throw new GrobidParsingException(
                "Failed to parse Grobid TEI XML response",
                e
            );
        }
    }

    private DocumentMetadata createMetadata(
        UUID documentId,
        GrobidData grobidData
    ) {
        var title = grobidData
            .getTeiHeader()
            .getFileDesc()
            .getTitleStmt()
            .getTitle();

        return DocumentMetadata.builder()
            .documentId(documentId)
            .title(title)
            .author(null)
            .abstractText(null)
            .additionalMetadata(Map.of("processEngine", "GROBID"))
            .build();
    }
}
