CREATE TABLE candidate_cvs (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    template_id VARCHAR(40) NOT NULL,
    language VARCHAR(10) NOT NULL DEFAULT 'vi',
    content_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    deleted_by UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_candidate_cvs_candidate_updated
    ON candidate_cvs(candidate_id, updated_at DESC)
    WHERE deleted_at IS NULL;
