package com.mochaeng.theia_api.processing.infrastructure.adapter.jpa;

import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "abstract")
    private String abstractText;

    @Column(name = "title_embedding", columnDefinition = "vector(768)")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Float[] titleEmbedding;

    @Column(name = "abstract_embedding", columnDefinition = "vector(768)")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private Float[] abstractEmbedding;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany
    @JoinTable(
        name = "document_author",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<AuthorEntity> authors = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public ProcessedDocument toDomain() {
        return ProcessedDocument.builder()
            .id(id)
            .title(title)
            .abstractText(abstractText)
            .titleEmbedding(
                titleEmbedding != null ? titleEmbedding.clone() : null
            )
            .abstractEmbedding(
                abstractEmbedding != null ? abstractEmbedding.clone() : null
            )
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    public static DocumentEntity fromDomain(ProcessedDocument document) {
        return DocumentEntity.builder()
            .id(document.id())
            .title(document.title())
            .abstractText(document.abstractText())
            .titleEmbedding(
                document.titleEmbedding() != null
                    ? document.titleEmbedding().clone()
                    : null
            )
            .abstractEmbedding(
                document.abstractEmbedding() != null
                    ? document.abstractEmbedding().clone()
                    : null
            )
            .createdAt(document.createdAt())
            .updatedAt(document.updatedAt())
            .build();
    }
}
