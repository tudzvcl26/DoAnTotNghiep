-- Test seed data: roles required by AuthenticationService.register()
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES
    (RANDOM_UUID(), 'ADMIN',     'System Administrator', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'EMPLOYER',  'Employer',             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (RANDOM_UUID(), 'CANDIDATE', 'Candidate',            CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
