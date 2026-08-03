CREATE TABLE ai_service.resume_documents
(
    id                       UUID PRIMARY KEY,
    owner_user_id            UUID          NOT NULL,
    bucket_name              VARCHAR(255)  NOT NULL,
    object_key               VARCHAR(1024) NOT NULL,
    original_filename        VARCHAR(255)  NOT NULL,
    content_type             VARCHAR(100)  NOT NULL,
    file_size                BIGINT        NOT NULL,
    checksum_sha256          VARCHAR(64)   NOT NULL,
    extracted_text           TEXT          NOT NULL,
    status                   VARCHAR(30)   NOT NULL,
    extraction_duration_ms   BIGINT        NOT NULL,
    upload_time              TIMESTAMP     NOT NULL,
    entity_version           BIGINT        NOT NULL DEFAULT 0,
    created_at               TIMESTAMP     NOT NULL,
    updated_at               TIMESTAMP     NOT NULL,

    CONSTRAINT uq_resume_documents_object_key UNIQUE (object_key),
    CONSTRAINT ck_resume_documents_file_size CHECK (file_size > 0 AND file_size <= 10485760),
    CONSTRAINT ck_resume_documents_status CHECK (status IN ('READY', 'ANALYZED', 'FAILED')),
    CONSTRAINT ck_resume_documents_extraction_duration CHECK (extraction_duration_ms >= 0)
);

CREATE TABLE ai_service.resume_analysis_results
(
    id                          UUID PRIMARY KEY,
    resume_document_id          UUID         NOT NULL,
    ai_task_id                  UUID         NOT NULL,
    prompt_template_version_id  UUID         NOT NULL,
    model_deployment_id         UUID         NOT NULL,
    provider_name               VARCHAR(100) NOT NULL,
    model_name                  VARCHAR(150) NOT NULL,
    prompt_version              VARCHAR(100) NOT NULL,
    structured_data             JSONB        NOT NULL,
    quality_score               INTEGER      NOT NULL,
    score_breakdown             JSONB        NOT NULL,
    input_tokens                BIGINT       NOT NULL DEFAULT 0,
    output_tokens               BIGINT       NOT NULL DEFAULT 0,
    analysis_duration_ms        BIGINT       NOT NULL,
    correlation_id              VARCHAR(100) NOT NULL,
    entity_version              BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,

    CONSTRAINT uq_resume_analysis_document UNIQUE (resume_document_id),
    CONSTRAINT fk_resume_analysis_document FOREIGN KEY (resume_document_id)
        REFERENCES ai_service.resume_documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_resume_analysis_task FOREIGN KEY (ai_task_id)
        REFERENCES ai_service.ai_tasks(id),
    CONSTRAINT fk_resume_analysis_prompt FOREIGN KEY (prompt_template_version_id)
        REFERENCES ai_service.prompt_template_versions(id),
    CONSTRAINT fk_resume_analysis_model FOREIGN KEY (model_deployment_id)
        REFERENCES ai_service.model_deployments(id),
    CONSTRAINT ck_resume_analysis_score CHECK (quality_score BETWEEN 0 AND 100),
    CONSTRAINT ck_resume_analysis_token_usage CHECK (input_tokens >= 0 AND output_tokens >= 0),
    CONSTRAINT ck_resume_analysis_duration CHECK (analysis_duration_ms >= 0)
);

CREATE TABLE ai_service.analysis_skill_items
(
    id                  UUID PRIMARY KEY,
    analysis_result_id  UUID         NOT NULL,
    skill_name          VARCHAR(255) NOT NULL,
    skill_category      VARCHAR(30)  NOT NULL,
    ordinal_position    INTEGER      NOT NULL,

    CONSTRAINT fk_analysis_skill_result FOREIGN KEY (analysis_result_id)
        REFERENCES ai_service.resume_analysis_results(id) ON DELETE CASCADE,
    CONSTRAINT ck_analysis_skill_category CHECK (skill_category IN ('GENERAL', 'TECHNICAL', 'SOFT')),
    CONSTRAINT ck_analysis_skill_position CHECK (ordinal_position >= 0),
    CONSTRAINT uq_analysis_skill UNIQUE (analysis_result_id, skill_name, skill_category)
);

CREATE TABLE ai_service.analysis_keyword_items
(
    id                  UUID PRIMARY KEY,
    analysis_result_id  UUID         NOT NULL,
    keyword             VARCHAR(255) NOT NULL,
    frequency           INTEGER      NOT NULL,
    ordinal_position    INTEGER      NOT NULL,

    CONSTRAINT fk_analysis_keyword_result FOREIGN KEY (analysis_result_id)
        REFERENCES ai_service.resume_analysis_results(id) ON DELETE CASCADE,
    CONSTRAINT ck_analysis_keyword_frequency CHECK (frequency > 0),
    CONSTRAINT ck_analysis_keyword_position CHECK (ordinal_position >= 0),
    CONSTRAINT uq_analysis_keyword UNIQUE (analysis_result_id, keyword)
);

CREATE INDEX idx_resume_documents_owner_created
    ON ai_service.resume_documents (owner_user_id, created_at DESC);
CREATE INDEX idx_resume_documents_checksum
    ON ai_service.resume_documents (owner_user_id, checksum_sha256);
CREATE INDEX idx_resume_analysis_created
    ON ai_service.resume_analysis_results (created_at DESC);
CREATE INDEX idx_analysis_skill_result
    ON ai_service.analysis_skill_items (analysis_result_id, ordinal_position);
CREATE INDEX idx_analysis_keyword_result
    ON ai_service.analysis_keyword_items (analysis_result_id, ordinal_position);

INSERT INTO ai_service.prompt_template_versions
    (id, template_code, version_number, system_prompt, user_prompt_template, output_schema,
     active, created_by, entity_version, created_at, updated_at)
SELECT
     'a1000000-0000-0000-0000-000000000001', 'RESUME_FACT_EXTRACTION', 1,
     'You extract facts from resumes. Return one valid JSON object only. Never score, rank, infer protected attributes, or invent missing facts. Use null or empty arrays when facts are absent.',
     'Extract the requested resume facts from the text below. Preserve factual wording and return only JSON.\n\nRESUME_TEXT:\n{{resumeText}}',
     '{"type":"object","required":["fullName","email","phone","location","linkedIn","portfolio","summary","education","experience","projects","skills","technicalSkills","softSkills","languages","certificates","achievements","keywords"]}',
     TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM ai_service.prompt_template_versions
    WHERE template_code = 'RESUME_FACT_EXTRACTION' AND active = TRUE
);

INSERT INTO ai_service.model_deployments
    (id, provider_name, model_name, deployment_name, capability, enabled,
     default_for_capability, configuration, entity_version, created_at, updated_at)
SELECT
     'a2000000-0000-0000-0000-000000000001', 'openai', 'gpt-4.1-mini',
     'resume-structured-default', 'STRUCTURED_GENERATION', TRUE, TRUE,
     '{"temperature":0,"responseFormat":"json_object"}', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM ai_service.model_deployments
    WHERE capability = 'STRUCTURED_GENERATION' AND enabled = TRUE AND default_for_capability = TRUE
);
