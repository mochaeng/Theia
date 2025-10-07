package com.mochaeng.theia_api.validator.infrastructure.adapter.pdfbox;

import com.mochaeng.theia_api.validator.application.port.out.ValidateDocumentStructurePort;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.action.*;
import org.springframework.stereotype.Component;

@Component
public class PdfboxValidateDocumentStructure
    implements ValidateDocumentStructurePort {

    @Override
    public Option<StructureError> validate(byte[] content) {
        return Try.withResources(() -> Loader.loadPDF(content))
            .of(this::validateDocument)
            .getOrElse(error("failed to load pdf"));
    }

    private Option<StructureError> validateDocument(PDDocument doc) {
        return validateEncryption(doc)
            .orElse(() -> validateCatalog(doc.getDocumentCatalog()))
            .orElse(() -> validatePages(doc.getDocumentCatalog()))
            .orElse(() -> validateOpenAction(doc.getDocumentCatalog()))
            .orElse(() ->
                validateDocumentActions(doc.getDocumentCatalog().getActions())
            )
            .orElse(() -> validatePageCount(doc));
    }

    private Option<StructureError> validateOpenAction(
        PDDocumentCatalog catalog
    ) {
        return Try.of(() -> {
            var action = catalog.getOpenAction();
            return switch (action) {
                case PDActionJavaScript $ -> error("open action is javascript");
                case PDActionLaunch $ -> error("open action is launch");
                case PDActionURI $ -> error("open action is uri");
                case PDActionSubmitForm $ -> error(
                    "open action is submit form"
                );
                case PDActionImportData $ -> error(
                    "open action is import data"
                );
                case null, default -> Option.<StructureError>none();
            };
        })
            .recover(ex ->
                error(
                    "unexpected error while validating open action: " +
                    ex.getMessage()
                )
            )
            .get();
    }

    private Option<StructureError> validateDocumentActions(
        PDDocumentCatalogAdditionalActions actions
    ) {
        if (actions == null) {
            return Option.none();
        }

        return Stream.of(
            actions.getWC(),
            actions.getWS(),
            actions.getDS(),
            actions.getWP(),
            actions.getDP()
        )
            .filter(Objects::nonNull)
            .findFirst()
            .map($ ->
                new StructureError("potential document-level action detected")
            )
            .map(Option::of)
            .orElse(Option.none());
    }

    private Option<StructureError> validateEncryption(PDDocument document) {
        return Option.when(document.isEncrypted(), () ->
            new StructureError("document is encrypted")
        );
    }

    private Option<StructureError> validatePages(PDDocumentCatalog catalog) {
        return Option.when(catalog != null && catalog.getPages() == null, () ->
            new StructureError("catalog has no pages entry")
        );
    }

    private Option<StructureError> validatePageCount(PDDocument doc) {
        return Option.when(doc.getNumberOfPages() == 0, () ->
            new StructureError("document is empty")
        );
    }

    private Option<StructureError> validateCatalog(PDDocumentCatalog catalog) {
        return Option.when(catalog == null, () ->
            new StructureError("catalog is empty")
        );
    }

    private Option<StructureError> error(String message) {
        return Option.of(new StructureError(message));
    }
}
