package com.mochaeng.theia_api.ingestion.application.service;

import com.mochaeng.theia_api.ingestion.application.port.in.AcceptDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.in.UploadIncomingDocumentUseCase;
import com.mochaeng.theia_api.ingestion.application.port.out.FileStoragePort;
import com.mochaeng.theia_api.ingestion.application.port.out.PublishIncomingDocumentPort;
import com.mochaeng.theia_api.ingestion.domain.model.Document;
import com.mochaeng.theia_api.shared.application.dto.DocumentMessage;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadIncomingDocumentService
    implements UploadIncomingDocumentUseCase {

    @Value("${storage.s3.incoming-bucket-name}")
    private String incomingBucket;

    private final AcceptDocumentUseCase validator;
    private final FileStoragePort fileStorage;
    private final PublishIncomingDocumentPort publisher;

    @Override
    public Either<UploadError, DocumentMessage> upload(Document document) {
        var validate = validator.validate(document);
        if (validate.isLeft()) {
            return Either.left(
                new UploadError(
                    "failed to validate document: " + validate.getLeft()
                )
            );
        }

        var filePath = fileStorage.storeDocument(
            incomingBucket,
            document.filename(),
            document
        );
        if (filePath.isLeft()) {
            return Either.left(
                new UploadError(
                    "failed to store document: " + filePath.getLeft().message()
                )
            );
        }

        var event = DocumentMessage.create(
            document,
            incomingBucket,
            filePath.get()
        );
        var publishResult = publisher.publishSync(event);
        if (publishResult.isLeft()) {
            return Either.left(
                new UploadError(
                    "failed to publish event: " +
                    publishResult.getLeft().message()
                )
            );
        }

        log.info(
            "document uploaded process completed for: {} with id: {}",
            document.filename(),
            document.id()
        );

        return Either.right(event);
    }
}
