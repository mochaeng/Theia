package com.mochaeng.theia_api.query.application.port.out;

import com.mochaeng.theia_api.shared.application.error.EmbeddingGenerationError;
import io.vavr.control.Either;
import java.util.List;

public interface GenerateQueryEmbeddingPort {
    Either<EmbeddingGenerationError, List<Float>> generate(String text);
}
