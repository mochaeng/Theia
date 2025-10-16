package com.mochaeng.theia_api.processing.infrastructure.adapter.grobid;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mochaeng.theia_api.processing.application.dto.ExtractDocumentResult;
import com.mochaeng.theia_api.processing.application.port.out.ExtractDocumentDataPort;
import com.mochaeng.theia_api.processing.application.port.out.ParseGrobidResponsePort;
import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidParsingException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidTimeoutException;
import com.mochaeng.theia_api.processing.infrastructure.adapter.grobid.exception.GrobidUnavailableException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final ParseGrobidResponsePort parseGrobidResponse;

    private final GrobidProperties grobidProperties;

    @Override
    public ExtractDocumentResult extract(
        UUID documentID,
        byte[] documentBytes
    ) {
        log.info(
            "starting Grobid extraction for document with id [{}]",
            documentID
        );

        try {
            var teiXml = callGrobidWithRetry(documentID, documentBytes);

            var metadata = parseGrobidResponse.parse(teiXml);
            if (metadata.isEmpty()) {
                return ExtractDocumentResult.failure(
                    "",
                    "metadata extraction failed"
                );
            }

            log.info(
                "successfully extracted metadata '{}' for document with id [{}]",
                metadata.get(),
                documentID
            );
            return ExtractDocumentResult.success(metadata.get());
        } catch (GrobidUnavailableException e) {
            log.error("grobid is unavailable: {}", e.getMessage());
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                "Service unreachable"
            );
        } catch (GrobidTimeoutException e) {
            log.error(
                "timeout calling grobid for document with id [{}]: {}",
                documentID,
                e.getMessage()
            );
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                "Request to Grobid timed out"
            );
        } catch (GrobidParsingException e) {
            log.error(
                "grobid parsing fail for document with id [{}]: {}",
                documentID,
                e.getMessage()
            );
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                "Grobid parsing failure"
            );
        } catch (GrobidException e) {
            log.error(
                "grobid extraction failed for document with id [{}]: {}",
                documentID,
                e.getMessage()
            );
            return ExtractDocumentResult.failure(
                e.getErrorCode(),
                e.getMessage()
            );
        } catch (Exception e) {
            log.error(
                "unexpected error extraction for document with id [{}]: {}",
                documentID,
                e.getMessage()
            );
            return ExtractDocumentResult.failure(
                "UNEXPECTED_ERROR",
                e.getMessage()
            );
        }
    }

    private String callGrobidWithRetry(UUID documentID, byte[] documentBytes) {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                log.info(
                    "retrying Grobid call for document with id [{}] (attempt {})",
                    documentID,
                    context.getRetryCount() + 1
                );
            }

            try {
                return makeGrobidCall(documentID, documentBytes);
            } catch (ResourceAccessException e) {
                log.warn(
                    "network error calling grobid for document with id [{}] (attempt {}): {}",
                    documentID,
                    context.getRetryCount(),
                    e.getMessage()
                );
                throw e;
            }
        });
    }

    private String makeGrobidCall(UUID documentID, byte[] documentBytes) {
        log.debug(
            "making http call to grobid for document with id [{}]",
            documentID
        );

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder
            .part("input", documentBytes)
            .filename("document.pdf")
            .contentType(MediaType.APPLICATION_PDF);
        builder.part(
            "consolidateHeader",
            grobidProperties.consolidateHeader().getValue()
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
            log.debug("resourceAccessException for grobid: {}", e.getMessage());

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

    //    private GrobidData parseTeiResponse(String teiXml) {
    //        try {
    //            log.debug("parsing tei xml response: {}", teiXml);
    //
    //            return xmlMapper.readValue(teiXml, GrobidData.class);
    //        } catch (Exception e) {
    //            log.error(
    //                "failed to parse grobid tei xml response: {}",
    //                e.getMessage()
    //            );
    //            throw new GrobidParsingException(
    //                "Failed to parse Grobid TEI XML response",
    //                e
    //            );
    //        }
    //    }

    private DocumentMetadata createMetadata(
        UUID documentId,
        GrobidData grobidData
    ) {
        var title = grobidData
            .getTeiHeader()
            .getFileDesc()
            .getTitleStmt()
            .getTitle();

        //        var abstract_ = grobidData
        //            .getTeiHeader()
        //            .getFileDesc()

        return DocumentMetadata.builder()
            //            .documentId(documentId)
            .title(title)
            .authors(new ArrayList<>())
            .abstractText(null)
            .additionalMetadata(Map.of("processEngine", "GROBID"))
            .build();
    }
}
