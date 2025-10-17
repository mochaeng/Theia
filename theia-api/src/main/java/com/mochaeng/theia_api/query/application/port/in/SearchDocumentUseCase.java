package com.mochaeng.theia_api.query.application.port.in;

import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.SearchResults;
import io.vavr.control.Either;

public interface SearchDocumentUseCase {
    Either<SearchError, SearchResults> search(SearchQuery query);

    sealed interface SearchError permits InvalidInput, General {}

    record InvalidInput(String message) implements SearchError {}

    record General(String message) implements SearchError {}
}
