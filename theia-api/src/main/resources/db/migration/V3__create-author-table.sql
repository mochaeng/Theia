CREATE TABLE author (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_author_email ON author(email);

CREATE TRIGGER update_author_updated_at
    BEFORE UPDATE ON author
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
