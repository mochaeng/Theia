package com.mochaeng.theia_api.query.application.port.out;

import com.mochaeng.theia_api.query.application.service.errors.SearchDocumentError;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.DocumentSearch;
import io.vavr.control.Either;
import java.util.List;

public interface SearchDocumentPort {
    Either<SearchDocumentError, List<DocumentSearch>> searchSimilarity(
        SearchQuery query
    );
}
