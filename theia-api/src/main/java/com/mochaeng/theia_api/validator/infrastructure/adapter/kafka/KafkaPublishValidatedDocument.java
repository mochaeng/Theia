package com.mochaeng.theia_api.validator.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.shared.application.dto.ValidatedDocumentMessage;
import com.mochaeng.theia_api.validator.application.port.out.PublishValidatedDocumentPort;
import io.vavr.control.Either;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublishValidatedDocument
    implements PublishValidatedDocumentPort {

    @Override
    public Either<PublishValidatedDocumentError, Void> publish(
        ValidatedDocumentMessage message
    ) {
        return null;
    }
}
