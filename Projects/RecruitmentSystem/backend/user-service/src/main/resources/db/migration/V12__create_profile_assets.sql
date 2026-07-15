SET search_path TO user_service;

CREATE TABLE profile_assets
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    certificate_id UUID,

    asset_kind VARCHAR(30) NOT NULL,

    storage_key VARCHAR(1024) NOT NULL,

    original_filename VARCHAR(255) NOT NULL,

    content_type VARCHAR(100) NOT NULL,

    size_bytes BIGINT NOT NULL,

    checksum VARCHAR(128),

    public_url VARCHAR(2048),

    asset_status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_profile_assets_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_profile_assets_certificate
        FOREIGN KEY (certificate_id)
            REFERENCES certificates(id)
            ON DELETE SET NULL
);

CREATE INDEX idx_profile_assets_profile_kind_status
    ON profile_assets(profile_id, asset_kind, asset_status);

CREATE INDEX idx_profile_assets_certificate_id
    ON profile_assets(certificate_id);

CREATE INDEX idx_profile_assets_storage_key
    ON profile_assets(storage_key);