package com.mochaeng.theia_api.processing.infrastructure.adapter.jpa;

import com.mochaeng.theia_api.processing.domain.model.Author;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAuthorRepository
    extends JpaRepository<AuthorEntity, Integer> {
    Optional<AuthorEntity> findByEmail(String email);
}
