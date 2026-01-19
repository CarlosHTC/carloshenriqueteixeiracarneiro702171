-- =========================
-- TABELA: ARTISTA
-- =========================
CREATE TABLE artista (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_artista_nome ON artista (nome);
CREATE INDEX idx_artista_tipo ON artista (tipo);

-- =========================
-- TABELA: ALBUM
-- =========================
CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    artista_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_artista
        FOREIGN KEY (artista_id)
        REFERENCES artista (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_album_nome ON album (nome);
CREATE INDEX idx_album_artista ON album (artista_id);
