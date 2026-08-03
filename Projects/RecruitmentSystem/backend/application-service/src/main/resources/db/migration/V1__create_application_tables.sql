CREATE TABLE applications
(
    id                 UUID PRIMARY KEY,
    candidate_id       UUID        NOT NULL,
    job_id             UUID        NOT NULL,
    company_id         UUID        NOT NULL,
    resume_snapshot_id UUID,
    job_snapshot_id    UUID,
    status             VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    matching_score     NUMERIC(5, 2),
    matching_version   VARCHAR(50),
    cover_letter       TEXT,
    active             BOOLEAN     NOT NULL DEFAULT TRUE,
    applied_at         TIMESTAMP   NOT NULL,
    created_at         TIMESTAMP   NOT NULL,
    updated_at         TIMESTAMP   NOT NULL,

    CONSTRAINT uq_application_candidate_job
        UNIQUE (candidate_id, job_id)
);

CREATE TABLE resume_snapshots
(
    id               UUID      PRIMARY KEY,
    application_id   UUID      NOT NULL UNIQUE,
    candidate_id     UUID      NOT NULL,
    snapshot_data    JSONB     NOT NULL,
    resume_version   VARCHAR(20),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,

    CONSTRAINT fk_resume_snapshot_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
);

CREATE TABLE job_snapshots
(
    id               UUID      PRIMARY KEY,
    application_id   UUID      NOT NULL UNIQUE,
    job_id           UUID      NOT NULL,
    snapshot_data    JSONB     NOT NULL,
    job_version      VARCHAR(20),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,

    CONSTRAINT fk_job_snapshot_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
);

CREATE TABLE application_status_histories
(
    id             UUID        PRIMARY KEY,
    application_id UUID        NOT NULL,
    from_status    VARCHAR(30),
    to_status      VARCHAR(30) NOT NULL,
    reason_code    VARCHAR(50),
    reason_detail  TEXT,
    changed_by     UUID        NOT NULL,
    changed_at     TIMESTAMP   NOT NULL,
    created_at     TIMESTAMP   NOT NULL,
    updated_at     TIMESTAMP   NOT NULL,

    CONSTRAINT fk_status_history_application
        FOREIGN KEY (application_id)
            REFERENCES applications (id)
);

CREATE INDEX idx_applications_candidate
    ON applications (candidate_id);

CREATE INDEX idx_applications_job
    ON applications (job_id);

CREATE INDEX idx_applications_company
    ON applications (company_id);

CREATE INDEX idx_applications_status
    ON applications (status);

CREATE INDEX idx_applications_active
    ON applications (active);

CREATE INDEX idx_status_history_application
    ON application_status_histories (application_id);

CREATE INDEX idx_status_history_changed_at
    ON application_status_histories (changed_at);

CREATE INDEX idx_resume_snapshot_candidate
    ON resume_snapshots (candidate_id);

CREATE INDEX idx_job_snapshot_job
    ON job_snapshots (job_id);
