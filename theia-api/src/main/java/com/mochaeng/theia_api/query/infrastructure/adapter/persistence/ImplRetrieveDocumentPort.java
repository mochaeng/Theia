package com.mochaeng.theia_api.query.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.domain.model.Author;
import com.mochaeng.theia_api.query.application.error.RetrieveDocumentError;
import com.mochaeng.theia_api.query.application.port.out.RetrieveDocumentPort;
import com.mochaeng.theia_api.query.application.web.dto.SearchQuery;
import com.mochaeng.theia_api.query.domain.model.DocumentSearch;
import com.mochaeng.theia_api.shared.infrastructure.jpa.JpaFieldRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImplRetrieveDocumentPort implements RetrieveDocumentPort {

    private final JpaFieldRepository fieldRepository;

    @Override
    public Either<
        RetrieveDocumentError,
        List<DocumentSearch>
    > searchBySimilarity(SearchQuery query) {
        return Try.of(() -> {
            var similarDocuments = fieldRepository.findSimilarDocuments(
                query.embedding(),
                query.fieldType(),
                query.threshold(),
                query.limit()
            );

            log.info(
                "similar documents return from database: {}",
                similarDocuments
            );

            if (similarDocuments.isEmpty()) {
                return Collections.<DocumentSearch>emptyList();
            }

            var documentsIds = similarDocuments
                .stream()
                .map(JpaFieldRepository.DocumentSimilarityResult::getDocumentId)
                .toList();

            var displayFields = List.of("title", "abstract");
            var displayData = fieldRepository.getDocumentDisplayData(
                documentsIds,
                displayFields
            );

            return buildDocumentSearchResults(similarDocuments, displayData);
        })
            .toEither()
            .mapLeft(t ->
                new RetrieveDocumentError.ServiceUnavailableError(
                    "Failed to retrieve documents: " + t
                )
            );
    }

    private List<DocumentSearch> buildDocumentSearchResults(
        List<JpaFieldRepository.DocumentSimilarityResult> similarDocuments,
        List<JpaFieldRepository.DocumentDisplayResult> displayData
    ) {
        var dataByDocument = displayData
            .stream()
            .collect(
                Collectors.groupingBy(
                    JpaFieldRepository.DocumentDisplayResult::getId
                )
            );

        var accumulator = new LinkedHashMap<UUID, DocumentSearchAccumulator>();

        for (var document : similarDocuments) {
            var id = document.getDocumentId();
            var data = dataByDocument.get(id);
            if (data != null && !data.isEmpty()) {
                var firstRow = data.getFirst();
                var title = data
                    .stream()
                    .filter(d -> "title".equals(d.getFieldType()))
                    .map(JpaFieldRepository.DocumentDisplayResult::getTitle)
                    .findFirst()
                    .orElse("Unknown title");

                accumulator.put(
                    id,
                    new DocumentSearchAccumulator(
                        id,
                        title,
                        document.getMaxSimilarity(),
                        firstRow.getFilePath(),
                        firstRow.getCreatedAt(),
                        firstRow.getUpdatedAt()
                    )
                );

                data
                    .stream()
                    .filter(d -> d.getAuthorFirstName() != null)
                    .forEach(d ->
                        accumulator
                            .get(id)
                            .addAuthor(
                                d.getAuthorFirstName(),
                                d.getAuthorLastName(),
                                d.getAuthorEmail()
                            )
                    );
            }
        }

        return accumulator
            .values()
            .stream()
            .map(DocumentSearchAccumulator::build)
            .collect(Collectors.toList());
    }

    @Getter
    @RequiredArgsConstructor
    private static class DocumentSearchAccumulator {

        private final UUID id;
        private final String title;
        private final Float similarity;
        private final String filePath;
        private final Set<Author> authors = new LinkedHashSet<>();
        private final Instant createdAt;
        private final Instant updatedAt;

        void addAuthor(String firstName, String lastName, String email) {
            if (firstName != null && email != null) {
                authors.add(new Author(firstName, lastName, email));
            }
        }

        DocumentSearch build() {
            return new DocumentSearch(
                id,
                title,
                similarity,
                new ArrayList<>(authors),
                filePath,
                createdAt,
                updatedAt
            );
        }
    }
}
