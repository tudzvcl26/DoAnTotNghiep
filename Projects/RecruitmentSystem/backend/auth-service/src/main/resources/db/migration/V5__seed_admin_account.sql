-- =====================================================
-- Version : V5
-- Description : Seed default admin account
-- =====================================================

INSERT INTO users
(
    id,
    email,
    password_hash,
    full_name,
    enabled,
    verified,
    created_at,
    updated_at
)
VALUES
    (
        gen_random_uuid(),
        'admin@recruitment.local',
        '$2a$10$lypOytbbiJi5UKVn8zK6p.VyDLVxxPMTCIhCTw3z2A2ThGN7Ydq.u',
        'System Administrator',
        TRUE,
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (email)
DO NOTHING;

INSERT INTO user_roles
(
    user_id,
    role_id
)
SELECT
    u.id,
    r.id
FROM users u
         JOIN roles r
              ON r.name='ADMIN'
WHERE u.email='admin@recruitment.local'
    ON CONFLICT DO NOTHING;