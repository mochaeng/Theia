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
            df.document_id,
            MAX(1 - (df.embedding <=> CAST(:embedding AS vector))) as max_similarity
        FROM document_field df
        WHERE df.field_type IN (:fieldTypes)
        GROUP BY df.document_id
        HAVING MAX(1 - (df.embedding <=> CAST(:embedding AS vector))) >= :threshold
        ORDER BY max_similarity DESC
        LIMIT :limit
        """,
        nativeQuery = true
    )
    List<DocumentSimilarityResult> findSimilarDocuments(
        @Param("embedding") float[] embedding,
        @Param("fieldTypes") List<String> fieldTypes,
        @Param("threshold") float threshold,
        @Param("limit") int limit
    );

    interface DocumentSimilarityResult {
        UUID getDocumentId();
        Float getMaxSimilarity();
    }

    @Query(
        value = """
        SELECT
            d.id,
            d.file_path,
            d.created_at,
            d.updated_at,
            df.field_type,
            df.field_text,
            a.first_name,
            a.last_name,
            a.email
        FROM document d
        LEFT JOIN document_field df ON d.id = df.document_id
            AND df.field_type IN (:displayFields)
        LEFT JOIN document_author da ON d.id = da.document_id
        LEFT JOIN author a ON da.author_id = a.id
        WHERE d.id IN (:documentIds)
        ORDER BY d.id, df.field_type
        """,
        nativeQuery = true
    )
    List<DocumentDisplayResult> getDocumentDisplayData(
        @Param("documentIds") List<UUID> documentIds,
        @Param("displayFields") List<String> displayFields
    );

    interface DocumentDisplayResult {
        UUID getId();

        @Value("#{target.field_text}")
        String getTitle();

        String getFilePath();
        String getFieldType();

        String getAuthorFirstName();
        String getAuthorLastName();
        String getAuthorEmail();
        Instant getCreatedAt();
        Instant getUpdatedAt();
    }
}
