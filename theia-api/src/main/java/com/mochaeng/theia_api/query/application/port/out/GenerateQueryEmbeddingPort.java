package com.mochaeng.theia_api.query.application.port.out;

import com.mochaeng.theia_api.query.application.service.errors.GenerateQueryEmbeddingError;
import io.vavr.control.Either;
import java.util.List;

public interface GenerateQueryEmbeddingPort {
    Either<GenerateQueryEmbeddingError, List<Float>> generate(String text);
}
