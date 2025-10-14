package com.mochaeng.theia_api.processing.application.port.out;

import com.mochaeng.theia_api.processing.domain.model.DocumentMetadata;
import io.vavr.control.Option;

public interface ParseGrobidResponsePort {
    Option<DocumentMetadata> parse(String response);
}
