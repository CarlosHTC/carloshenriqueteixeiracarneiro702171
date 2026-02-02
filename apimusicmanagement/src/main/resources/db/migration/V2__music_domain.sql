-- =========================
-- V2 - Evolucao do dominio musical
-- =========================

-- ARTISTA: adiciona optimistic lock
ALTER TABLE artista
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- ALBUM: adiciona updated_at + optimistic lock
ALTER TABLE album
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- =========================
-- TABELA: GENERO
-- =========================
CREATE TABLE IF NOT EXISTS genero (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_genero_nome UNIQUE (nome)
);

CREATE INDEX IF NOT EXISTS idx_genero_nome ON genero (nome);

-- =========================
-- TABELA: FAIXA
-- =========================
CREATE TABLE IF NOT EXISTS faixa (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL,
    numero INT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    duracao_segundos INT NOT NULL,
    explicita BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_faixa_album
        FOREIGN KEY (album_id) REFERENCES album (id) ON DELETE CASCADE,
    CONSTRAINT uk_faixa_album_numero UNIQUE (album_id, numero)
);

CREATE INDEX IF NOT EXISTS idx_faixa_album ON faixa (album_id);
CREATE INDEX IF NOT EXISTS idx_faixa_titulo ON faixa (titulo);

-- =========================
-- N:N ALBUM <-> GENERO
-- =========================
CREATE TABLE IF NOT EXISTS album_genero (
    album_id BIGINT NOT NULL,
    genero_id BIGINT NOT NULL,
    PRIMARY KEY (album_id, genero_id),
    CONSTRAINT fk_album_genero_album
        FOREIGN KEY (album_id) REFERENCES album (id) ON DELETE CASCADE,
    CONSTRAINT fk_album_genero_genero
        FOREIGN KEY (genero_id) REFERENCES genero (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_album_genero_genero ON album_genero (genero_id);
