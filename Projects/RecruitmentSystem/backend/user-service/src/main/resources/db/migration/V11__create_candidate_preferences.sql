SET search_path TO user_service;

CREATE TABLE candidate_preferences
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL UNIQUE,

    salary_minimum NUMERIC(19,2),

    salary_maximum NUMERIC(19,2),

    salary_currency VARCHAR(3),

    salary_period VARCHAR(30),

    availability_status VARCHAR(30) NOT NULL,

    work_arrangement VARCHAR(30),

    recommendation_consent BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_candidate_preferences_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_salary_range
        CHECK (
            salary_maximum IS NULL
                OR salary_minimum IS NULL
                OR salary_maximum >= salary_minimum
            )
);

CREATE INDEX idx_candidate_preferences_availability
    ON candidate_preferences(availability_status);

CREATE INDEX idx_candidate_preferences_work_arrangement
    ON candidate_preferences(work_arrangement);