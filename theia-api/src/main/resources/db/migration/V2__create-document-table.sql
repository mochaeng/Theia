CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document (
    id UUID PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL,
    file_hash BYTEA,
--    file_size BIGINT NOT NULL,
    title TEXT,
    abstract TEXT,
    title_embedding VECTOR(768),
    abstract_embedding VECTOR(768),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_document_file_hash_unique ON
    document(file_hash);

CREATE INDEX idx_document_title_embedding ON
    document USING ivfflat(title_embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX idx_document_abstract_embedding ON
    document USING ivfflat(abstract_embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE TRIGGER update_document_updated_at
    BEFORE UPDATE ON document
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();