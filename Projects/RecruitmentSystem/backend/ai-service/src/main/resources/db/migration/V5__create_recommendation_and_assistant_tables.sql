CREATE TABLE ai_service.job_recommendations
(
    id                          UUID PRIMARY KEY,
    match_result_id             UUID         NOT NULL,
    resume_document_id          UUID         NOT NULL,
    candidate_user_id           UUID         NOT NULL,
    job_id                      UUID         NOT NULL,
    overall_score               INTEGER      NOT NULL,
    ai_task_id                  UUID         NOT NULL,
    prompt_template_version_id  UUID         NOT NULL,
    model_deployment_id         UUID         NOT NULL,
    provider_name               VARCHAR(100) NOT NULL,
    model_name                  VARCHAR(150) NOT NULL,
    prompt_version              VARCHAR(100) NOT NULL,
    recommendation_data         JSONB        NOT NULL,
    input_tokens                BIGINT       NOT NULL DEFAULT 0,
    output_tokens               BIGINT       NOT NULL DEFAULT 0,
    generation_duration_ms      BIGINT       NOT NULL,
    correlation_id              VARCHAR(100) NOT NULL,
    entity_version              BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,

    CONSTRAINT uq_job_recommendation_match UNIQUE (match_result_id),
    CONSTRAINT fk_job_recommendation_match FOREIGN KEY (match_result_id)
        REFERENCES ai_service.job_match_results(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_recommendation_resume FOREIGN KEY (resume_document_id)
        REFERENCES ai_service.resume_documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_recommendation_task FOREIGN KEY (ai_task_id) REFERENCES ai_service.ai_tasks(id),
    CONSTRAINT fk_job_recommendation_prompt FOREIGN KEY (prompt_template_version_id)
        REFERENCES ai_service.prompt_template_versions(id),
    CONSTRAINT fk_job_recommendation_model FOREIGN KEY (model_deployment_id)
        REFERENCES ai_service.model_deployments(id),
    CONSTRAINT ck_job_recommendation_score CHECK (overall_score BETWEEN 0 AND 100),
    CONSTRAINT ck_job_recommendation_tokens CHECK (input_tokens >= 0 AND output_tokens >= 0),
    CONSTRAINT ck_job_recommendation_duration CHECK (generation_duration_ms >= 0)
);

CREATE TABLE ai_service.candidate_recommendations
(
    id                          UUID PRIMARY KEY,
    match_result_id             UUID         NOT NULL,
    job_id                      UUID         NOT NULL,
    job_owner_user_id           UUID         NOT NULL,
    resume_document_id          UUID         NOT NULL,
    candidate_user_id           UUID         NOT NULL,
    overall_score               INTEGER      NOT NULL,
    ai_task_id                  UUID         NOT NULL,
    prompt_template_version_id  UUID         NOT NULL,
    model_deployment_id         UUID         NOT NULL,
    provider_name               VARCHAR(100) NOT NULL,
    model_name                  VARCHAR(150) NOT NULL,
    prompt_version              VARCHAR(100) NOT NULL,
    recommendation_data         JSONB        NOT NULL,
    input_tokens                BIGINT       NOT NULL DEFAULT 0,
    output_tokens               BIGINT       NOT NULL DEFAULT 0,
    generation_duration_ms      BIGINT       NOT NULL,
    correlation_id              VARCHAR(100) NOT NULL,
    entity_version              BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,

    CONSTRAINT uq_candidate_recommendation_match UNIQUE (match_result_id),
    CONSTRAINT fk_candidate_recommendation_match FOREIGN KEY (match_result_id)
        REFERENCES ai_service.job_match_results(id) ON DELETE CASCADE,
    CONSTRAINT fk_candidate_recommendation_resume FOREIGN KEY (resume_document_id)
        REFERENCES ai_service.resume_documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_candidate_recommendation_task FOREIGN KEY (ai_task_id) REFERENCES ai_service.ai_tasks(id),
    CONSTRAINT fk_candidate_recommendation_prompt FOREIGN KEY (prompt_template_version_id)
        REFERENCES ai_service.prompt_template_versions(id),
    CONSTRAINT fk_candidate_recommendation_model FOREIGN KEY (model_deployment_id)
        REFERENCES ai_service.model_deployments(id),
    CONSTRAINT ck_candidate_recommendation_score CHECK (overall_score BETWEEN 0 AND 100),
    CONSTRAINT ck_candidate_recommendation_tokens CHECK (input_tokens >= 0 AND output_tokens >= 0),
    CONSTRAINT ck_candidate_recommendation_duration CHECK (generation_duration_ms >= 0)
);

CREATE TABLE ai_service.assistant_sessions
(
    id                  UUID PRIMARY KEY,
    requested_by        UUID         NOT NULL,
    assistant_type      VARCHAR(30)  NOT NULL,
    task_type           VARCHAR(100) NOT NULL,
    job_id              UUID,
    resume_document_id  UUID,
    match_result_id     UUID,
    correlation_id      VARCHAR(100) NOT NULL,
    entity_version      BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,

    CONSTRAINT fk_assistant_session_match FOREIGN KEY (match_result_id)
        REFERENCES ai_service.job_match_results(id) ON DELETE SET NULL,
    CONSTRAINT ck_assistant_session_type CHECK (assistant_type IN ('CANDIDATE', 'RECRUITER'))
);

CREATE TABLE ai_service.assistant_responses
(
    id                          UUID PRIMARY KEY,
    session_id                  UUID         NOT NULL,
    ai_task_id                  UUID         NOT NULL,
    prompt_template_version_id  UUID         NOT NULL,
    model_deployment_id         UUID         NOT NULL,
    provider_name               VARCHAR(100) NOT NULL,
    model_name                  VARCHAR(150) NOT NULL,
    prompt_version              VARCHAR(100) NOT NULL,
    response_data               JSONB        NOT NULL,
    input_tokens                BIGINT       NOT NULL DEFAULT 0,
    output_tokens               BIGINT       NOT NULL DEFAULT 0,
    generation_duration_ms      BIGINT       NOT NULL,
    correlation_id              VARCHAR(100) NOT NULL,
    entity_version              BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,

    CONSTRAINT uq_assistant_response_session UNIQUE (session_id),
    CONSTRAINT fk_assistant_response_session FOREIGN KEY (session_id)
        REFERENCES ai_service.assistant_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_assistant_response_task FOREIGN KEY (ai_task_id) REFERENCES ai_service.ai_tasks(id),
    CONSTRAINT fk_assistant_response_prompt FOREIGN KEY (prompt_template_version_id)
        REFERENCES ai_service.prompt_template_versions(id),
    CONSTRAINT fk_assistant_response_model FOREIGN KEY (model_deployment_id)
        REFERENCES ai_service.model_deployments(id),
    CONSTRAINT ck_assistant_response_tokens CHECK (input_tokens >= 0 AND output_tokens >= 0),
    CONSTRAINT ck_assistant_response_duration CHECK (generation_duration_ms >= 0)
);

CREATE INDEX idx_job_recommendation_candidate_score
    ON ai_service.job_recommendations (candidate_user_id, resume_document_id, overall_score DESC);
CREATE INDEX idx_candidate_recommendation_job_score
    ON ai_service.candidate_recommendations (job_owner_user_id, job_id, overall_score DESC);
CREATE INDEX idx_assistant_session_requester_created
    ON ai_service.assistant_sessions (requested_by, created_at DESC);

INSERT INTO ai_service.prompt_template_versions
    (id, template_code, version_number, system_prompt, user_prompt_template, output_schema,
     active, created_by, entity_version, created_at, updated_at)
VALUES
    ('a4000000-0000-0000-0000-000000000001', 'JOB_RECOMMENDATION', 1,
     'Explain why a published job is or is not recommended using only the supplied deterministic match. Never calculate, change, predict, or output a score. Never make employment decisions or infer protected attributes. Return one JSON object only.',
     'Create a concise candidate-facing recommendation from this immutable structured context:\n{{context}}',
     '{"type":"object","required":["recommendationSummary","gapSummary","recommendationReason"]}',
     TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('a4000000-0000-0000-0000-000000000002', 'CANDIDATE_RECOMMENDATION', 1,
     'Explain candidate alignment using only the supplied deterministic match. Never calculate, change, predict, or output a score. Do not approve or reject a candidate and do not infer protected attributes. Return one JSON object only.',
     'Create a concise recruiter-facing recommendation from this immutable structured context:\n{{context}}',
     '{"type":"object","required":["recommendationSummary","interviewRecommendation","recommendationReason"]}',
     TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('a4000000-0000-0000-0000-000000000003', 'RECRUITER_ASSISTANT', 1,
     'Perform only the requested recruiter task from supplied structured project data. Never calculate or change scores, approve or reject candidates, change business state, infer protected attributes, or act as a chatbot. Hiring recommendations are advisory evidence summaries only. Return one JSON object only.',
     'Task: {{task}}\nStructured context:\n{{context}}',
     '{"type":"object","required":["summary","recommendations","risks","nextSteps"]}',
     TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('a4000000-0000-0000-0000-000000000004', 'CANDIDATE_ASSISTANT', 1,
     'Perform only the requested candidate career task from supplied structured project data. Never calculate or change scores, make employment decisions, change business state, infer protected attributes, or act as a chatbot. Return one JSON object only.',
     'Task: {{task}}\nStructured context:\n{{context}}',
     '{"type":"object","required":["summary","recommendations","risks","nextSteps"]}',
     TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (template_code, version_number) DO NOTHING;
