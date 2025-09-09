package com.mochaeng.theia_api.processing.infrastructure.adapter.jpa;

import com.mochaeng.theia_api.processing.domain.model.ProcessedDocument;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import lombok.*;
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
        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        DocumentEntity that = (DocumentEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
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
