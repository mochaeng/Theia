package com.mochaeng.theia_api.query.application.web;

import com.mochaeng.theia_api.query.application.port.in.SearchDocumentUseCase;
import com.mochaeng.theia_api.query.application.service.errors.DocumentSearchError;
import com.mochaeng.theia_api.query.application.web.dto.QueryRequest;
import com.mochaeng.theia_api.query.application.web.dto.QueryResponse;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.shared.dto.ErrorResponse;
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
@RequestMapping("api/v1")
public class QueryController {

    private final SearchDocumentUseCase searchDocument;

    @PostMapping(
        value = "/search",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> queryDocument(
        @Valid @RequestBody QueryRequest request
    ) {
        log.info(
            "received search request for query: '{}' in field: '{}'",
            request.query(),
            request.fields()
        );

        var searchQuery = SearchQuery.of(
            request.query(),
            request.fields(),
            request.limit(),
            request.threshold()
        );

        return searchDocument
            .search(searchQuery)
            .fold(this::toResponse, search -> {
                var response = QueryResponse.from(search);
                return ResponseEntity.ok(response);
            });
    }

    private ResponseEntity<ErrorResponse> toResponse(DocumentSearchError e) {
        return switch (e) {
            case DocumentSearchError.InvalidInputError(
                var msg
            ) -> ResponseEntity.badRequest().body(
                new ErrorResponse(msg, "/v1/search")
            );
            case null, default -> ResponseEntity.badRequest().body(
                new ErrorResponse("", "/v1/search")
            );
        };
    }
}
