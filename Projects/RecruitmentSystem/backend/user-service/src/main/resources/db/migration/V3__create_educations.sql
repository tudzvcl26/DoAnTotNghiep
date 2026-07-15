SET search_path TO user_service;

CREATE TABLE educations
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    institution_name VARCHAR(255) NOT NULL,

    qualification VARCHAR(150) NOT NULL,

    field_of_study VARCHAR(200),

    start_date DATE NOT NULL,

    end_date DATE,

    grade VARCHAR(50),

    description TEXT,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_educations_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_education_dates
        CHECK (
            end_date IS NULL
                OR end_date >= start_date
            )
);

CREATE INDEX idx_educations_profile_id
    ON educations(profile_id);

CREATE INDEX idx_educations_profile_start_date
    ON educations(profile_id, start_date);

CREATE INDEX idx_educations_institution
    ON educations(institution_name);

CREATE INDEX idx_educations_field_of_study
    ON educations(field_of_study);

CREATE INDEX idx_educations_start_date
    ON educations(start_date);