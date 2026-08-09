DROP INDEX IF EXISTS idx_refresh_token_token;

ALTER TABLE refresh_tokens
    RENAME COLUMN token TO token_hash;

UPDATE refresh_tokens
SET token_hash = encode(digest(token_hash, 'sha256'), 'hex');

ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash TYPE VARCHAR(64);

CREATE INDEX idx_refresh_token_hash
    ON refresh_tokens(token_hash);
