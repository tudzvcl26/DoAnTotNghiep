SET search_path TO user_service;

CREATE TABLE career_objectives
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL UNIQUE,

    objective_text TEXT,

    target_seniority VARCHAR(100),

    availability_status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_career_objectives_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_career_objectives_profile_id
    ON career_objectives(profile_id);

CREATE INDEX idx_career_objectives_availability
    ON career_objectives(availability_status);