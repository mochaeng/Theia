package com.mochaeng.theia_api.query.application.service;

import com.mochaeng.theia_api.query.application.port.in.SearchDocumentUseCase;
import com.mochaeng.theia_api.query.application.port.out.GenerateQueryEmbeddingPort;
import com.mochaeng.theia_api.query.application.port.out.RetrieveDocumentPort;
import com.mochaeng.theia_api.query.application.service.errors.DocumentSearchError;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.Search;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchDocumentService implements SearchDocumentUseCase {

    private final GenerateQueryEmbeddingPort generateQueryEmbedding;
    private final RetrieveDocumentPort searchDocument;

    @Override
    public Either<DocumentSearchError, Search> search(SearchQuery query) {
        var startTime = System.currentTimeMillis();

        log.info(
            "starting document search for query '{}' in field '{}'",
            query.query(),
            query.fieldType()
        );

        return SearchQuery.validateQuery(query)
            .flatMap(validQuery ->
                generateQueryEmbedding
                    .generate(validQuery.query())
                    .mapLeft(DocumentSearchError.toDomainError())
                    .map(validQuery::withEmbedding)
            )
            .flatMap(queryWithEmbedding ->
                searchDocument
                    .searchBySimilarity(queryWithEmbedding)
                    .mapLeft(DocumentSearchError.toDomainError())
            )
            .peekLeft(documentSearchError ->
                log.info(
                    "Document search failed: {}",
                    documentSearchError.message()
                )
            )
            .map(documentSearches -> {
                var queryTime = System.currentTimeMillis() - startTime;

                log.info("document search finished in {} ms", queryTime);
                log.info("Similar documents: {}", documentSearches);

                return new Search(
                    documentSearches,
                    documentSearches.size(),
                    queryTime
                );
            });
    }
}
