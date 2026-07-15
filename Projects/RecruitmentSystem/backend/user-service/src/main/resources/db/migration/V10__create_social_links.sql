SET search_path TO user_service;

CREATE TABLE social_links
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    link_type VARCHAR(30) NOT NULL,

    url VARCHAR(2048) NOT NULL,

    label VARCHAR(150),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_social_links_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_social_links_profile_type
        UNIQUE(profile_id, link_type)
);

CREATE INDEX idx_social_links_profile_id
    ON social_links(profile_id);