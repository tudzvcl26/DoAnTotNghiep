SET search_path TO user_service;

CREATE TABLE profile_skills
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    skill_id UUID NOT NULL,

    skill_level VARCHAR(30) NOT NULL,

    years_experience NUMERIC(4,1),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_profile_skills_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_profile_skills_skill
        FOREIGN KEY (skill_id)
            REFERENCES skills(id),

    CONSTRAINT uk_profile_skills_profile_skill
        UNIQUE(profile_id, skill_id)
);

CREATE INDEX idx_profile_skills_profile_id
    ON profile_skills(profile_id);

CREATE INDEX idx_profile_skills_skill_id
    ON profile_skills(skill_id);