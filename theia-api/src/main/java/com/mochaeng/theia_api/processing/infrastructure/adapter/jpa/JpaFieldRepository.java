package com.mochaeng.theia_api.processing.infrastructure.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFieldRepository
    extends JpaRepository<FieldEntity, Integer> {}
