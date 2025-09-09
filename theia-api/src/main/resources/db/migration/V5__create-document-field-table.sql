CREATE TABLE document_field (
    id BIGSERIAL PRIMARY KEY,
    document_id UUID REFERENCES document(id) ON DELETE CASCADE,
    field_type VARCHAR(50) NOT NULL,
    field_text TEXT NOT NULL,
    embedding VECTOR(768) NOT NULL,
    token_count INT,
    model VARCHAR(100),
    processing_time_ms BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    UNIQUE(document_id, field_type)
);

CREATE INDEX idx_document_field_id ON document_field(id);
CREATE INDEX idx_document_field_type ON document_field(field_type);
CREATE INDEX idx_document_field_embedding ON
    document_field USING ivfflat(embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE TRIGGER update_document_field_updated_at
    BEFORE UPDATE ON document_field
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
