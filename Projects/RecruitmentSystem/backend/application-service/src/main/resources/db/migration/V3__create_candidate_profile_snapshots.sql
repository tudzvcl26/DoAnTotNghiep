CREATE TABLE candidate_profile_snapshots
(
    id              UUID         PRIMARY KEY,
    application_id  UUID         NOT NULL UNIQUE,
    candidate_id    UUID         NOT NULL,
    profile_id      UUID,
    display_name    VARCHAR(150) NOT NULL,
    headline        VARCHAR(255),
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(30),
    profile_version BIGINT,
    captured_at     TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,

    CONSTRAINT fk_candidate_profile_snapshot_application
        FOREIGN KEY (application_id) REFERENCES applications (id)
);

ALTER TABLE applications
    ADD COLUMN candidate_profile_snapshot_id UUID;

ALTER TABLE applications
    ADD CONSTRAINT fk_application_candidate_profile_snapshot
        FOREIGN KEY (candidate_profile_snapshot_id) REFERENCES candidate_profile_snapshots (id);

CREATE INDEX idx_candidate_profile_snapshot_candidate
    ON candidate_profile_snapshots (candidate_id);
