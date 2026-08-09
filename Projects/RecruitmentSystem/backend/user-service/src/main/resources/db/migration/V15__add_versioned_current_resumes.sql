SET search_path TO user_service;

ALTER TABLE profile_assets
    ADD COLUMN asset_version BIGINT,
    ADD COLUMN is_current BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_profile_assets_current_resume
    ON profile_assets(profile_id)
    WHERE asset_kind = 'RESUME'
      AND is_current = TRUE
      AND asset_status = 'ACTIVE'
      AND deleted_at IS NULL;

CREATE UNIQUE INDEX uq_profile_assets_resume_version
    ON profile_assets(profile_id, asset_version)
    WHERE asset_kind = 'RESUME' AND asset_version IS NOT NULL;

ALTER TABLE profile_assets
    ADD CONSTRAINT ck_profile_assets_resume_version
        CHECK ((asset_kind = 'RESUME' AND asset_version IS NOT NULL AND asset_version > 0)
            OR (asset_kind <> 'RESUME' AND asset_version IS NULL));
