package com.mochaeng.theia_api.processing.infrastructure.adapter.persistence;

import com.mochaeng.theia_api.processing.domain.model.Author;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.AuthorEntity;
import com.mochaeng.theia_api.processing.infrastructure.adapter.jpa.JpaAuthorRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorPersistenceService {

    private final JpaAuthorRepository authorRepository;

    public Set<AuthorEntity> processAuthors(List<Author> authorEntities) {
        return authorEntities
            .stream()
            .map(this::findOrCreateAuthor)
            .collect(Collectors.toSet());
    }

    private AuthorEntity findOrCreateAuthor(Author author) {
        return authorRepository
            .findByEmail(author.email())
            .orElseGet(() -> createNewAuthor(author));
    }

    private AuthorEntity createNewAuthor(Author author) {
        try {
            var authorEntity = AuthorEntity.builder()
                .firstName(author.firstName())
                .lastName(author.lastName())
                .email(author.email())
                .build();

            return authorRepository.save(authorEntity);
        } catch (DataIntegrityViolationException e) {
            log.debug(
                "Author with email {} already exists due to concurrent creation",
                author.email()
            );
            return authorRepository
                .findByEmail(author.email())
                .orElseThrow(() ->
                    new RuntimeException(
                        "Failed to retrieve author after creation conflict"
                    )
                );
        }
    }
}
