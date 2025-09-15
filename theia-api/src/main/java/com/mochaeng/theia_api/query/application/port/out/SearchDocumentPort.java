package com.mochaeng.theia_api.query.application.port.out;

import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.DocumentSearch;
import com.mochaeng.theia_api.shared.application.error.SimilaritySearchError;
import io.vavr.control.Either;
import java.util.List;

public interface SearchDocumentPort {
    Either<SimilaritySearchError, List<DocumentSearch>> searchBySimilarity(
        SearchQuery query
    );
}
