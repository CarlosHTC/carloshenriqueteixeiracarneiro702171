CREATE TABLE IF NOT EXISTS album_capa (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,

    object_key VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_album_capa_album
        FOREIGN KEY (album_id) REFERENCES album (id) ON DELETE CASCADE,
    CONSTRAINT uk_album_capa_album_object UNIQUE (album_id, object_key)
);

CREATE INDEX IF NOT EXISTS idx_album_capa_album ON album_capa (album_id);
CREATE INDEX IF NOT EXISTS idx_album_capa_object_key ON album_capa (object_key);
