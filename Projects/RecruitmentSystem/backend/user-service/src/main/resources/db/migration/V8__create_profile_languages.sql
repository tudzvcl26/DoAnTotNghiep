SET search_path TO user_service;

CREATE TABLE profile_languages
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    language_id UUID NOT NULL,

    language_level VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_profile_languages_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_profile_languages_language
        FOREIGN KEY (language_id)
            REFERENCES languages(id),

    CONSTRAINT uk_profile_languages_profile_language
        UNIQUE(profile_id, language_id)
);

CREATE INDEX idx_profile_languages_profile_id
    ON profile_languages(profile_id);

CREATE INDEX idx_profile_languages_language_id
    ON profile_languages(language_id);