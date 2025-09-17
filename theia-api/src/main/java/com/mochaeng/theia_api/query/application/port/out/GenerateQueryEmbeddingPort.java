package com.mochaeng.theia_api.query.application.port.out;

import com.mochaeng.theia_api.shared.application.error.EmbeddingError;
import io.vavr.control.Either;

public interface GenerateQueryEmbeddingPort {
    Either<EmbeddingError, float[]> generate(String text);
}
