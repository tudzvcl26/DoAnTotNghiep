-- V5 may already be deployed, so neutralize that known credential without rewriting history.
UPDATE users
SET password_hash = crypt(gen_random_uuid()::text, gen_salt('bf', 12)),
    enabled = FALSE,
    verified = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@recruitment.local';

DELETE FROM refresh_tokens
WHERE user_id IN (
    SELECT id FROM users WHERE email = 'admin@recruitment.local'
);
