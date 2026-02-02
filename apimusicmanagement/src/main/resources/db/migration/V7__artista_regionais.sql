CREATE TABLE artista_regional (
  artista_id BIGINT NOT NULL,
  regional_id BIGINT NOT NULL,
  PRIMARY KEY (artista_id, regional_id),
  CONSTRAINT fk_artista_regional_artista
    FOREIGN KEY (artista_id) REFERENCES artista(id),
  CONSTRAINT fk_artista_regional_regional
    FOREIGN KEY (regional_id) REFERENCES regional(id)
);

CREATE INDEX ix_artista_regional_regional_id ON artista_regional (regional_id);
CREATE INDEX ix_artista_regional_artista_id ON artista_regional (artista_id);