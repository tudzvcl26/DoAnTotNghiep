CREATE TABLE ai_service.job_match_results
(
    id                         UUID PRIMARY KEY,
    resume_analysis_result_id  UUID         NOT NULL,
    resume_document_id         UUID         NOT NULL,
    resume_owner_user_id       UUID         NOT NULL,
    job_id                     UUID         NOT NULL,
    job_company_id             UUID         NOT NULL,
    job_owner_user_id          UUID         NOT NULL,
    overall_score              INTEGER      NOT NULL,
    matched_skills             JSONB        NOT NULL,
    missing_skills             JSONB        NOT NULL,
    matched_keywords           JSONB        NOT NULL,
    missing_keywords           JSONB        NOT NULL,
    strengths                  JSONB        NOT NULL,
    weaknesses                 JSONB        NOT NULL,
    recommendations            JSONB        NOT NULL,
    gap_analysis               JSONB        NOT NULL,
    matched_experience         VARCHAR(500) NOT NULL,
    matched_education          VARCHAR(500) NOT NULL,
    rule_version               VARCHAR(100) NOT NULL,
    weights_version            VARCHAR(100) NOT NULL,
    weights_snapshot           JSONB        NOT NULL,
    matching_duration_ms       BIGINT       NOT NULL,
    correlation_id             VARCHAR(100) NOT NULL,
    entity_version             BIGINT       NOT NULL DEFAULT 0,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,

    CONSTRAINT uq_job_match_job_resume UNIQUE (job_id, resume_analysis_result_id),
    CONSTRAINT fk_job_match_resume_analysis FOREIGN KEY (resume_analysis_result_id)
        REFERENCES ai_service.resume_analysis_results(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_match_resume_document FOREIGN KEY (resume_document_id)
        REFERENCES ai_service.resume_documents(id) ON DELETE CASCADE,
    CONSTRAINT ck_job_match_score CHECK (overall_score BETWEEN 0 AND 100),
    CONSTRAINT ck_job_match_duration CHECK (matching_duration_ms >= 0)
);

CREATE TABLE ai_service.match_score_breakdowns
(
    id                  UUID PRIMARY KEY,
    match_result_id     UUID         NOT NULL,
    dimension_code      VARCHAR(50)  NOT NULL,
    maximum_score       INTEGER      NOT NULL,
    actual_score        INTEGER      NOT NULL,
    reason              VARCHAR(1000) NOT NULL,
    ordinal_position    INTEGER      NOT NULL,

    CONSTRAINT fk_match_breakdown_result FOREIGN KEY (match_result_id)
        REFERENCES ai_service.job_match_results(id) ON DELETE CASCADE,
    CONSTRAINT uq_match_breakdown_dimension UNIQUE (match_result_id, dimension_code),
    CONSTRAINT ck_match_breakdown_scores CHECK (
        maximum_score >= 0 AND actual_score >= 0 AND actual_score <= maximum_score
    ),
    CONSTRAINT ck_match_breakdown_position CHECK (ordinal_position >= 0)
);

CREATE INDEX idx_job_match_job_created
    ON ai_service.job_match_results (job_id, created_at DESC);
CREATE INDEX idx_job_match_resume_created
    ON ai_service.job_match_results (resume_document_id, created_at DESC);
CREATE INDEX idx_job_match_resume_owner
    ON ai_service.job_match_results (resume_owner_user_id, created_at DESC);
CREATE INDEX idx_job_match_job_owner
    ON ai_service.job_match_results (job_owner_user_id, created_at DESC);
CREATE INDEX idx_match_breakdown_result
    ON ai_service.match_score_breakdowns (match_result_id, ordinal_position);
