package com.mochaeng.theia_api.shared.infrastructure.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaFieldRepository
    extends JpaRepository<FieldEntity, Integer> {
    @Query(
        value = """
        SELECT
            d.id,
            df.field_text,
            1 - (df.embedding <=> CAST(:embedding AS vector)) as similarity,
            a.first_name,
            a.last_name,
            a.email,
            d.created_at,
            d.updated_at
        FROM document_field df
        INNER JOIN document d ON df.document_id = d.id
        LEFT JOIN document_author da ON d.id = da.document_id
        LEFT JOIN author a ON da.author_id = a.id
        WHERE df.field_type = :fieldType
        ORDER BY df.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """,
        nativeQuery = true
    )
    List<SearchBySimilarityProjection> searchBySimilarity(
        @Param("embedding") float[] embedding,
        @Param("fieldType") String fieldType,
        @Param("threshold") float threshold,
        @Param("limit") int limit
    );

    interface SearchBySimilarityProjection {
        UUID getId();

        @Value("#{target.field_text}")
        String getTitle();

        Float getSimilarity();
        String getAuthorFirstName();
        String getAuthorLastName();
        String getAuthorEmail();
        Instant getCreatedAt();
        Instant getUpdatedAt();
    }
}
