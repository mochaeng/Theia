package com.mochaeng.theia_api.processing.infrastructure.adapter.jpa;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.processing.SQL;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Builder
@Entity
@Table(name = "document_field")
@NoArgsConstructor
@AllArgsConstructor
public class FieldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "field_type")
    private String fieldType;

    @Column(name = "field_text")
    private String fieldText;

    @Column(name = "embedding", columnDefinition = "vector")
    private float[] embedding;

    @Column(name = "model")
    private String model;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "token_count")
    private Integer tokenCount;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

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
        FieldEntity that = (FieldEntity) o;
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
