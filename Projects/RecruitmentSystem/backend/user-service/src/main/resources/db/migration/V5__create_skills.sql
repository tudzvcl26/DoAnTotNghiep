SET search_path TO user_service;

CREATE TABLE skills
(
    id UUID PRIMARY KEY,

    normalized_skill_key VARCHAR(150) NOT NULL UNIQUE,

    display_name VARCHAR(150) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL
);

CREATE INDEX idx_skills_normalized_key
    ON skills(normalized_skill_key);