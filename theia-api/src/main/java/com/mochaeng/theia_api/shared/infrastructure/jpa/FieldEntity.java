package com.mochaeng.theia_api.shared.infrastructure.jpa;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

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

        Class<?> oEffectiveClass = Hibernate.getClass(o);
        Class<?> thisEffectiveClass = Hibernate.getClass(this);

        if (thisEffectiveClass != oEffectiveClass) return false;

        if (!(o instanceof FieldEntity that)) return false;

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
