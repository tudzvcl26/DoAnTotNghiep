-- =====================================================
-- Version : V4
-- Description : Seed Roles & Permissions
-- =====================================================

---------------------------------------------------------
-- ROLES
---------------------------------------------------------

INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'ADMIN', 'System Administrator', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'EMPLOYER', 'Employer', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'CANDIDATE', 'Candidate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (name) DO NOTHING;

---------------------------------------------------------
-- PERMISSIONS
---------------------------------------------------------

INSERT INTO permissions (id, name, description, created_at, updated_at)
VALUES

    (gen_random_uuid(),'USER_READ','Read user',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (gen_random_uuid(),'USER_CREATE','Create user',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (gen_random_uuid(),'USER_UPDATE','Update user',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (gen_random_uuid(),'USER_DELETE','Delete user',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),

    (gen_random_uuid(),'JOB_READ','Read job',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (gen_random_uuid(),'JOB_CREATE','Create job',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (gen_random_uuid(),'JOB_UPDATE','Update job',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),
    (gen_random_uuid(),'JOB_DELETE','Delete job',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)

    ON CONFLICT (name) DO NOTHING;

---------------------------------------------------------
-- ROLE PERMISSIONS
---------------------------------------------------------

INSERT INTO role_permissions(role_id, permission_id)

SELECT
    r.id,
    p.id

FROM roles r
         CROSS JOIN permissions p

WHERE

    r.name='ADMIN'

    ON CONFLICT DO NOTHING;