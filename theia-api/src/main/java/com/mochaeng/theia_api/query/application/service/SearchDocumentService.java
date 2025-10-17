package com.mochaeng.theia_api.query.application.service;

import com.mochaeng.theia_api.query.application.port.in.SearchDocumentUseCase;
import com.mochaeng.theia_api.query.application.port.out.GenerateQueryEmbeddingPort;
import com.mochaeng.theia_api.query.application.port.out.RetrieveDocumentPort;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.SearchResults;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchDocumentService implements SearchDocumentUseCase {

    private final GenerateQueryEmbeddingPort generator;
    private final RetrieveDocumentPort retriever;

    @Override
    public Either<SearchError, SearchResults> search(SearchQuery searchQuery) {
        log.info(
            "starting document search for text '{}' in field '{}'",
            searchQuery.text(),
            searchQuery.fieldTypes()
        );

        var queryResult = SearchQuery.validateQuery(searchQuery);
        if (queryResult.isLeft()) {
            return Either.left(
                new InvalidInput(
                    "text validation failed: " + queryResult.getLeft().message()
                )
            );
        }

        var startTime = System.currentTimeMillis();

        var embeddingsResult = generator.generate(queryResult.get().text());
        if (embeddingsResult.isLeft()) {
            return Either.left(
                new General(
                    "embeddings generation failed: " +
                    embeddingsResult.getLeft().message()
                )
            );
        }

        var query = queryResult.get().withEmbedding(embeddingsResult.get());
        var searchResult = retriever.BySimilarity(query);
        if (searchResult.isLeft()) {
            return Either.left(
                new General(
                    "search for query failed: " +
                    searchResult.getLeft().message()
                )
            );
        }

        var totalTime = System.currentTimeMillis() - startTime;

        return Either.right(
            SearchResults.builder()
                .results(searchResult.get())
                .totalResults(searchResult.get().size())
                .queryTimeMs(totalTime)
                .build()
        );
    }
}
