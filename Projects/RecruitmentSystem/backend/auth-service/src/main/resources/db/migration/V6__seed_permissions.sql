-- =====================================================
-- Version : V6
-- Description : Seed default permissions
-- =====================================================

INSERT INTO permissions(name,description)
VALUES
    ('USER_READ','Read user'),
    ('USER_WRITE','Write user'),

    ('COMPANY_READ','Read company'),
    ('COMPANY_WRITE','Write company'),

    ('JOB_READ','Read job'),
    ('JOB_WRITE','Write job'),

    ('APPLICATION_READ','Read application'),
    ('APPLICATION_WRITE','Write application'),

    ('ADMIN','Administrator')
    ON CONFLICT(name)
DO NOTHING;