package com.mochaeng.theia_api.shared.infrastructure.jpa;

import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

@Builder
@Getter
@Setter
@Entity
@Table(name = "document")
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    private UUID id;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_hash")
    private byte[] fileHash;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(
        mappedBy = "document",
        orphanRemoval = true,
        cascade = CascadeType.ALL
    )
    @Builder.Default
    private Set<FieldEntity> fields = new LinkedHashSet<>();

    @ManyToMany
    @Builder.Default
    @JoinTable(
        name = "document_author",
        joinColumns = @JoinColumn(name = "document_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<AuthorEntity> authors = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    //    public ProcessedDocument toDomain() {
    //        return ProcessedDocument.builder()
    //            .id(id)
    //            .createdAt(createdAt)
    //            .updatedAt(updatedAt)
    //            .build();
    //    }

    public static DocumentEntity fromDomain(ProcessedDocument document) {
        return DocumentEntity.builder()
            .id(document.id())
            .filePath(document.filePath())
            .fileHash(document.fileHash())
            .createdAt(document.createdAt())
            .updatedAt(document.updatedAt())
            .build();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = Hibernate.getClass(o);
        Class<?> thisEffectiveClass = Hibernate.getClass(this);

        if (thisEffectiveClass != oEffectiveClass) return false;

        if (!(o instanceof DocumentEntity that)) return false;

        return getId() != null && Objects.equals(getId(), that.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode()
            : getClass().hashCode();
    }
}
