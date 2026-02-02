CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(64) NOT NULL UNIQUE,
    username VARCHAR(120) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auth_refresh_token_username ON auth_refresh_token(username);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_token_expires ON auth_refresh_token(expires_at);