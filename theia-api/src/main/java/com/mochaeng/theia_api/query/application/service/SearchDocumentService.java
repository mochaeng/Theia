package com.mochaeng.theia_api.query.application.service;

import com.mochaeng.theia_api.query.application.port.in.SearchDocumentUseCase;
import com.mochaeng.theia_api.query.application.port.out.GenerateQueryEmbeddingPort;
import com.mochaeng.theia_api.query.application.port.out.SearchDocumentPort;
import com.mochaeng.theia_api.query.application.service.errors.DocumentSearchError;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.Search;
import com.mochaeng.theia_api.shared.application.error.EmbeddingGenerationError;
import com.mochaeng.theia_api.shared.application.error.SimilaritySearchError;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchDocumentService implements SearchDocumentUseCase {

    private final SearchDocumentPort searchDocument;
    private final GenerateQueryEmbeddingPort generateQueryEmbedding;

    @Override
    public Either<DocumentSearchError, Search> search(SearchQuery query) {
        long startTime = System.currentTimeMillis();

        log.info(
            "starting document search for query '{}' in field '{}'",
            query.query(),
            query.fieldType()
        );

        var queryEmbedding = generateQueryEmbedding.generate(query.query());
        if (queryEmbedding.isLeft()) {
            return Either.left(
                mapEmbeddingErrorToDomainError(
                    "failed to get embeddings",
                    queryEmbedding.getLeft()
                )
            );
        }

        var queryWithEmbeddings = query.withEmbedding(queryEmbedding.get());

        var documents = searchDocument.searchBySimilarity(queryWithEmbeddings);
        if (documents.isLeft()) {
            return Either.left(
                mapSimilaritySearchErrorToDomainError("failed to retrieve document from database",documents.getLeft())
            );
        }

        var endTime = System.currentTimeMillis();
        var queryTime = endTime - startTime;

        log.info(
            "document search completed in {}ms. Found {} results",
            queryTime,
            documents.get().size()
        );

        return Either.right(
            new Search(documents.get(), documents.get().size(), queryTime)
        );
    }

    private DocumentSearchError mapEmbeddingErrorToDomainError(
        String msg,
        EmbeddingGenerationError error
    ) {
        return switch (error) {
            case EmbeddingGenerationError.ServiceUnavailable e -> new DocumentSearchError.QueryError(
                msg + ":" + e.message(),
                e.details()
            );
            default -> new DocumentSearchError.UnknownError("", "");
        };
    }

    private DocumentSearchError mapSimilaritySearchErrorToDomainError(String msg,
        SimilaritySearchError error
    ) {
        return switch (error) {
            case SimilaritySearchError.ServiceUnavailableError e -> new DocumentSearchError.QueryError(
                msg + ":" + e.message(),
                e.details()
            );
            default -> new DocumentSearchError.UnknownError("", "");
        };
    }
}
