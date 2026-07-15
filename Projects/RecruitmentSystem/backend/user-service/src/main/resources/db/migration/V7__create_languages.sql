SET search_path TO user_service;

CREATE TABLE languages
(
    id UUID PRIMARY KEY,

    language_code VARCHAR(20) NOT NULL UNIQUE,

    display_name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL
);

CREATE INDEX idx_languages_code
    ON languages(language_code);