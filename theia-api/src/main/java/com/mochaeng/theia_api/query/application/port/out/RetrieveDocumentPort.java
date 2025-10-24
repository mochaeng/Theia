package com.mochaeng.theia_api.query.application.port.out;

import com.mochaeng.theia_api.query.application.error.RetrieveDocumentError;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.DocumentSearch;
import io.vavr.control.Either;
import java.util.List;

public interface RetrieveDocumentPort {
    Either<RetrieveDocumentError, List<DocumentSearch>> bySimilarity(
        SearchQuery query
    );
}
