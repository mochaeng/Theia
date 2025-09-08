CREATE TABLE document (
    id UUID PRIMARY KEY,
    file_path VARCHAR(500) NOT NULL,
    file_hash BYTEA NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_document_file_hash_unique ON document(file_hash);

CREATE TRIGGER update_document_updated_at
    BEFORE UPDATE ON document
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();