package com.mochaeng.theia_api.query.application.port.in;

import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.Search;

public interface SearchDocumentUseCase {
    Search search(SearchQuery query);
}
