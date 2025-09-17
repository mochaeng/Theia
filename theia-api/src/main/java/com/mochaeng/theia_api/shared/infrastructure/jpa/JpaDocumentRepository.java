package com.mochaeng.theia_api.shared.infrastructure.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDocumentRepository
    extends JpaRepository<DocumentEntity, UUID> {
    boolean existsByFileHash(byte[] hash);
}
