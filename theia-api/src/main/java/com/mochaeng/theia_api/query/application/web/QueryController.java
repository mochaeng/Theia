package com.mochaeng.theia_api.query.application.web;

import com.mochaeng.theia_api.query.application.port.in.SearchDocumentUseCase;
import com.mochaeng.theia_api.query.application.service.SearchDocumentService;
import com.mochaeng.theia_api.query.application.web.dto.QueryRequest;
import com.mochaeng.theia_api.query.application.web.dto.QueryResponse;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class QueryController {

    private final SearchDocumentUseCase searchDocument;

    @PostMapping(
        value = "/search",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<QueryResponse> queryDocument(
        @Valid @RequestBody QueryRequest request
    ) {
        log.info(
            "received search request for query: '{}' in field: '{}'",
            request.query(),
            request.field()
        );

        var searchQuery = SearchQuery.of(
            request.query(),
            request.field(),
            request.limit(),
            request.threshold()
        );

        var search = searchDocument.search(searchQuery);

        return null;
    }
}
