SET search_path TO user_service;

CREATE TABLE experiences
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    company_name VARCHAR(255) NOT NULL,

    position_title VARCHAR(255) NOT NULL,

    employment_type VARCHAR(30),

    work_location VARCHAR(255),

    start_date DATE NOT NULL,

    end_date DATE,

    is_current BOOLEAN NOT NULL DEFAULT FALSE,

    description TEXT,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_experiences_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_experience_dates
        CHECK (
            end_date IS NULL
                OR end_date >= start_date
            )
);

CREATE INDEX idx_experiences_profile_id
    ON experiences(profile_id);

CREATE INDEX idx_experiences_company
    ON experiences(company_name);

CREATE INDEX idx_experiences_current
    ON experiences(is_current);

CREATE INDEX idx_experiences_start_date
    ON experiences(start_date);