package com.mochaeng.theia_api.query.application.service;

import com.mochaeng.theia_api.query.application.port.in.SearchDocumentUseCase;
import com.mochaeng.theia_api.query.application.port.out.GenerateQueryEmbeddingPort;
import com.mochaeng.theia_api.query.application.port.out.SearchDocumentPort;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.Search;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchDocumentService implements SearchDocumentUseCase {

    //    private final SearchDocumentPort searchDocument;
    //    private final GenerateQueryEmbeddingPort generateQueryEmbedding;

    @Override
    public Search search(SearchQuery query) {
        //        long startTime = System.currentTimeMillis();
        //
        //        log.info(
        //            "starting document search for query: '{}' in field: '{}'",
        //            query.query(),
        //            query.fieldType()
        //        );
        //
        //        var queryEmbedding = generateQueryEmbedding
        //            .generate(query.query())
        //            .getOrElseThrow(error -> {
        //                log.error(
        //                    "failed to generate embeddings for query: {}",
        //                    error.message()
        //                );
        //                throw new SearchDocumentException(
        //                    SearchDocumentException.Code.QUERY_ERROR,
        //                    error.message()
        //                );
        //            });
        //
        //        var queryWithEmbeddings = query.withEmbedding(queryEmbedding);
        //
        //        var documents = searchDocument
        //            .searchSimilarity(queryWithEmbeddings)
        //            .getOrElseThrow(error -> {
        //                log.error(error.message(), error.code());
        //                throw new SearchDocumentException(
        //                    SearchDocumentException.Code.SIMILARITY_ERROR,
        //                    error.message()
        //                );
        //            });
        //
        //        var endTime = System.currentTimeMillis();
        //        var queryTime = endTime - startTime;
        //
        //        log.info(
        //            "document search completed in {}ms. Found {} results",
        //            queryTime,
        //            documents.size()
        //        );
        //
        //        return new Search(documents, documents.size(), queryTime);
        return null;
    }
}
