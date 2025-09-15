package com.mochaeng.theia_api.query.application.port.in;

import com.mochaeng.theia_api.query.application.service.errors.DocumentSearchError;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.Search;
import io.vavr.control.Either;

public interface SearchDocumentUseCase {
    Either<DocumentSearchError, Search> search(SearchQuery query);
}
