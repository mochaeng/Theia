package com.mochaeng.theia_api.processing.infrastructure.adapter.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface JpaDocumentRepository
    extends JpaRepository<DocumentJpaEntity, UUID> {}
