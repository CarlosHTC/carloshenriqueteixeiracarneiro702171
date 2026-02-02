CREATE TABLE artista_foto (
  id BIGSERIAL PRIMARY KEY,
  artista_id BIGINT NOT NULL UNIQUE,
  bucket VARCHAR(100) NOT NULL,
  object_key VARCHAR(512) NOT NULL,
  content_type VARCHAR(100),
  size_bytes BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_artista_foto_artista
    FOREIGN KEY (artista_id) REFERENCES artista(id)
);

CREATE INDEX ix_artista_foto_artista_id ON artista_foto (artista_id);
