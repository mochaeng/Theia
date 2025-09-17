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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImplRetrieveDocumentPort implements RetrieveDocumentPort {

    private final JpaFieldRepository fieldRepository;

    @Override
    public Either<
        RetrieveDocumentError,
        List<DocumentSearch>
    > searchBySimilarity(SearchQuery query) {
        return Try.of(() -> {
            var results = fieldRepository.searchBySimilarity(
                query.embedding(),
                query.fieldType(),
                query.threshold(),
                query.limit()
            );

            var accumulator = new LinkedHashMap<
                UUID,
                DocumentSearchAccumulator
            >();

            for (var row : results) {
                var documentID = row.getId();
                accumulator
                    .computeIfAbsent(documentID, id ->
                        new DocumentSearchAccumulator(
                            id,
                            row.getTitle(),
                            row.getSimilarity(),
                            row.getCreatedAt(),
                            row.getUpdatedAt()
                        )
                    )
                    .addAuthor(
                        row.getAuthorFirstName(),
                        row.getAuthorLastName(),
                        row.getAuthorEmail()
                    );
            }

            return accumulator
                .values()
                .stream()
                .map(DocumentSearchAccumulator::build)
                .collect(Collectors.toList());
        })
            .toEither()
            .mapLeft(t ->
                new RetrieveDocumentError.ServiceUnavailableError(
                    "Failed to retrieve documents: " + t.getMessage()
                )
            );
    }

    @Getter
    @RequiredArgsConstructor
    private static class DocumentSearchAccumulator {

        private final UUID id;
        private final String title;
        private final Float similarity;
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
                createdAt,
                updatedAt
            );
        }
    }
}
