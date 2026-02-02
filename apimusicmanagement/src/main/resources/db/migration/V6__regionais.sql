CREATE TABLE regional (
  id BIGSERIAL PRIMARY KEY,
  external_id BIGINT NOT NULL,
  nome VARCHAR(200) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  inativado_em TIMESTAMP NULL
);

-- Garante que exista no máximo 1 registro ATIVO por external_id,
-- permitindo histórico (inativos) conforme regra do edital.
CREATE UNIQUE INDEX ux_regional_external_id_ativo
ON regional (external_id)
WHERE ativo = true;

CREATE INDEX ix_regional_ativo ON regional (ativo);
CREATE INDEX ix_regional_nome ON regional (nome);
