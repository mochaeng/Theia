package com.mochaeng.theia_api.validator.infrastructure.adapter.kafka;

import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import com.mochaeng.theia_api.shared.infrastructure.kafka.KafkaPublishHelper;
import com.mochaeng.theia_api.validator.application.port.out.PublishValidatedDocumentPort;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaPublishValidatedDocument
    implements PublishValidatedDocumentPort {

    private final KafkaPublishHelper publishHelper;
    private final String validatedDocumentTopic;

    public KafkaPublishValidatedDocument(
        KafkaPublishHelper publishHelper,
        @Value(
            "${kafka.topics.document-validated}"
        ) String validatedDocumentTopic
    ) {
        this.publishHelper = publishHelper;
        this.validatedDocumentTopic = validatedDocumentTopic;
    }

    @Override
    public Option<PublishValidatedDocumentError> publish(
        DocumentMessage message
    ) {
        var key = message.documentID().toString();
        log.info("publishing validated document with id '{}'", key);

        return publishHelper
            .publishAsync(validatedDocumentTopic, key, message)
            .failed()
            .map(Throwable::getMessage)
            .map(PublishValidatedDocumentError::new)
            .toOption();
    }
}
