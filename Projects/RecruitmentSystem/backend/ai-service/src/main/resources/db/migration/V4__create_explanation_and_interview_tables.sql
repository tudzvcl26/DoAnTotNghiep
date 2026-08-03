CREATE TABLE ai_service.ai_match_explanations
(
    id                          UUID PRIMARY KEY,
    match_result_id             UUID         NOT NULL,
    ai_task_id                  UUID         NOT NULL,
    prompt_template_version_id  UUID         NOT NULL,
    model_deployment_id         UUID         NOT NULL,
    provider_name               VARCHAR(100) NOT NULL,
    model_name                  VARCHAR(150) NOT NULL,
    prompt_version              VARCHAR(100) NOT NULL,
    explanation_data            JSONB        NOT NULL,
    input_tokens                BIGINT       NOT NULL DEFAULT 0,
    output_tokens               BIGINT       NOT NULL DEFAULT 0,
    generation_duration_ms      BIGINT       NOT NULL,
    correlation_id              VARCHAR(100) NOT NULL,
    entity_version              BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,

    CONSTRAINT uq_ai_match_explanation_match UNIQUE (match_result_id),
    CONSTRAINT fk_ai_match_explanation_match FOREIGN KEY (match_result_id)
        REFERENCES ai_service.job_match_results(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_match_explanation_task FOREIGN KEY (ai_task_id)
        REFERENCES ai_service.ai_tasks(id),
    CONSTRAINT fk_ai_match_explanation_prompt FOREIGN KEY (prompt_template_version_id)
        REFERENCES ai_service.prompt_template_versions(id),
    CONSTRAINT fk_ai_match_explanation_model FOREIGN KEY (model_deployment_id)
        REFERENCES ai_service.model_deployments(id),
    CONSTRAINT ck_ai_match_explanation_tokens CHECK (input_tokens >= 0 AND output_tokens >= 0),
    CONSTRAINT ck_ai_match_explanation_duration CHECK (generation_duration_ms >= 0)
);

CREATE TABLE ai_service.interview_question_sets
(
    id                          UUID PRIMARY KEY,
    match_result_id             UUID         NOT NULL,
    ai_task_id                  UUID         NOT NULL,
    prompt_template_version_id  UUID         NOT NULL,
    model_deployment_id         UUID         NOT NULL,
    provider_name               VARCHAR(100) NOT NULL,
    model_name                  VARCHAR(150) NOT NULL,
    prompt_version              VARCHAR(100) NOT NULL,
    question_data               JSONB        NOT NULL,
    input_tokens                BIGINT       NOT NULL DEFAULT 0,
    output_tokens               BIGINT       NOT NULL DEFAULT 0,
    generation_duration_ms      BIGINT       NOT NULL,
    correlation_id              VARCHAR(100) NOT NULL,
    entity_version              BIGINT       NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP    NOT NULL,
    updated_at                  TIMESTAMP    NOT NULL,

    CONSTRAINT uq_interview_question_set_match UNIQUE (match_result_id),
    CONSTRAINT fk_interview_question_set_match FOREIGN KEY (match_result_id)
        REFERENCES ai_service.job_match_results(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_question_set_task FOREIGN KEY (ai_task_id)
        REFERENCES ai_service.ai_tasks(id),
    CONSTRAINT fk_interview_question_set_prompt FOREIGN KEY (prompt_template_version_id)
        REFERENCES ai_service.prompt_template_versions(id),
    CONSTRAINT fk_interview_question_set_model FOREIGN KEY (model_deployment_id)
        REFERENCES ai_service.model_deployments(id),
    CONSTRAINT ck_interview_question_set_tokens CHECK (input_tokens >= 0 AND output_tokens >= 0),
    CONSTRAINT ck_interview_question_set_duration CHECK (generation_duration_ms >= 0)
);

CREATE INDEX idx_ai_match_explanations_created
    ON ai_service.ai_match_explanations (created_at DESC);
CREATE INDEX idx_interview_question_sets_created
    ON ai_service.interview_question_sets (created_at DESC);

INSERT INTO ai_service.prompt_template_versions
    (id, template_code, version_number, system_prompt, user_prompt_template, output_schema,
     active, created_by, entity_version, created_at, updated_at)
SELECT
    'a3000000-0000-0000-0000-000000000001', 'MATCH_EXPLANATION', 1,
    'Explain the supplied deterministic recruitment match. Never calculate, change, predict, or restate a different score. Treat all scores, breakdowns, matched items, and gaps as immutable facts. Do not infer protected attributes. Return one JSON object only.',
    'Create a factual matching explanation, resume improvement plan, gap priorities, and career learning advice from this immutable context:\n{{context}}',
    '{"type":"object","required":["overallEvaluation","strengths","weaknesses","highScoreReasons","lowScoreReasons","missingTechnologies","careerSuggestions","resumeImprovementChecklist","skillRecommendations","projectRecommendations","certificationSuggestions","keywordImprovements","experienceImprovements","educationImprovements","gapExplanations","learningRoadmap","recommendedTechnologies","recommendedCertifications","portfolioImprovements"]}',
    TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM ai_service.prompt_template_versions
    WHERE template_code = 'MATCH_EXPLANATION' AND active = TRUE
);

INSERT INTO ai_service.prompt_template_versions
    (id, template_code, version_number, system_prompt, user_prompt_template, output_schema,
     active, created_by, entity_version, created_at, updated_at)
SELECT
    'a3000000-0000-0000-0000-000000000002', 'INTERVIEW_PREPARATION', 1,
    'Generate interview preparation grounded only in the supplied resume facts, published job, and deterministic match. Never calculate or change scores. Do not infer protected attributes. Return one JSON object only.',
    'Generate technical, behavioral, HR, and project interview questions at easy, medium, and hard difficulty from this context:\n{{context}}',
    '{"type":"object","required":["technicalQuestions","behavioralQuestions","hrQuestions","projectQuestions"]}',
    TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM ai_service.prompt_template_versions
    WHERE template_code = 'INTERVIEW_PREPARATION' AND active = TRUE
);
