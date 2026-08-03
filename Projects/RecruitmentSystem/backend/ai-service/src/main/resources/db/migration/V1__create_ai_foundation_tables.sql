CREATE SCHEMA IF NOT EXISTS ai_service;

CREATE TABLE ai_service.ai_tasks
(
    id                  UUID PRIMARY KEY,
    task_type           VARCHAR(100) NOT NULL,
    status              VARCHAR(30)  NOT NULL,
    requested_by        UUID         NOT NULL,
    subject_type        VARCHAR(50),
    subject_id          UUID,
    correlation_id      VARCHAR(100) NOT NULL,
    idempotency_key     VARCHAR(100),
    progress            INTEGER      NOT NULL DEFAULT 0,
    retry_count         INTEGER      NOT NULL DEFAULT 0,
    input_checksum      VARCHAR(128),
    provider_name       VARCHAR(100),
    model_name          VARCHAR(150),
    prompt_version      VARCHAR(100),
    result_reference    VARCHAR(1024),
    error_code          VARCHAR(100),
    error_message       TEXT,
    retryable           BOOLEAN      NOT NULL DEFAULT FALSE,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    entity_version      BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,

    CONSTRAINT ck_ai_tasks_status CHECK (
        status IN ('PENDING', 'RUNNING', 'COMPLETED', 'PARTIAL', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT ck_ai_tasks_progress CHECK (progress BETWEEN 0 AND 100),
    CONSTRAINT ck_ai_tasks_retry_count CHECK (retry_count >= 0),
    CONSTRAINT uq_ai_tasks_request_idempotency UNIQUE (requested_by, idempotency_key)
);

CREATE TABLE ai_service.prompt_template_versions
(
    id                   UUID PRIMARY KEY,
    template_code        VARCHAR(100) NOT NULL,
    version_number       INTEGER      NOT NULL,
    system_prompt        TEXT         NOT NULL,
    user_prompt_template TEXT         NOT NULL,
    output_schema        JSONB,
    active               BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by           UUID,
    entity_version       BIGINT       NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,

    CONSTRAINT ck_prompt_template_version_positive CHECK (version_number > 0),
    CONSTRAINT uq_prompt_template_code_version UNIQUE (template_code, version_number)
);

CREATE TABLE ai_service.model_deployments
(
    id                       UUID PRIMARY KEY,
    provider_name            VARCHAR(100) NOT NULL,
    model_name               VARCHAR(150) NOT NULL,
    deployment_name          VARCHAR(150) NOT NULL,
    capability               VARCHAR(50)  NOT NULL,
    enabled                  BOOLEAN      NOT NULL DEFAULT FALSE,
    default_for_capability   BOOLEAN      NOT NULL DEFAULT FALSE,
    configuration            JSONB,
    entity_version           BIGINT       NOT NULL DEFAULT 0,
    created_at               TIMESTAMP    NOT NULL,
    updated_at               TIMESTAMP    NOT NULL,

    CONSTRAINT ck_model_deployment_capability CHECK (
        capability IN ('STRUCTURED_GENERATION', 'EMBEDDING')
    ),
    CONSTRAINT uq_model_deployment_identity UNIQUE (
        provider_name, model_name, deployment_name, capability
    )
);

CREATE INDEX idx_ai_tasks_requested_created
    ON ai_service.ai_tasks (requested_by, created_at DESC);
CREATE INDEX idx_ai_tasks_status_created
    ON ai_service.ai_tasks (status, created_at);
CREATE INDEX idx_ai_tasks_subject
    ON ai_service.ai_tasks (subject_type, subject_id);
CREATE UNIQUE INDEX uq_prompt_template_active
    ON ai_service.prompt_template_versions (template_code)
    WHERE active = TRUE;
CREATE INDEX idx_model_deployments_capability_enabled
    ON ai_service.model_deployments (capability, enabled);
CREATE UNIQUE INDEX uq_model_deployment_default_capability
    ON ai_service.model_deployments (capability)
    WHERE default_for_capability = TRUE AND enabled = TRUE;
