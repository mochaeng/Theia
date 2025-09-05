CREATE TABLE document_author (
    document_id UUID REFERENCES document(id) ON DELETE CASCADE,
    author_id INTEGER REFERENCES author(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    PRIMARY KEY (document_id, author_id)
);

CREATE INDEX idx_document_author_author_id ON
    document_author(author_id);

CREATE TRIGGER update_document_author_updated_at
    BEFORE UPDATE ON document_author
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();