-- Additive: existing synchronous routes and persisted explanation shape remain valid.
ALTER TABLE ai_service.ai_tasks ADD COLUMN input_payload JSONB;
CREATE INDEX idx_ai_tasks_generation_queue ON ai_service.ai_tasks (task_type, status, created_at);

UPDATE ai_service.prompt_template_versions SET active = FALSE, updated_at = CURRENT_TIMESTAMP
WHERE template_code = 'MATCH_EXPLANATION';
INSERT INTO ai_service.prompt_template_versions
 (id, template_code, version_number, system_prompt, user_prompt_template, output_schema,
  active, created_by, entity_version, created_at, updated_at)
VALUES ('a6000000-0000-0000-0000-000000000001', 'MATCH_EXPLANATION', 3,
 'Giải thích ngắn gọn bằng tiếng Việt dựa đúng dữ kiện. Không tính lại điểm hoặc bịa kinh nghiệm. Không suy đoán thông tin nhạy cảm. Giữ nguyên tên riêng và công nghệ. Chỉ viết một câu đánh giá và tối đa một hành động cụ thể cho mỗi danh sách. Không nhắc lại toàn bộ bảng điểm. Gợi ý học tập là đề xuất, không phải kinh nghiệm đã có.',
 'Dữ kiện đối chiếu bất biến:\n{{context}}',
 '{"type":"object","required":["overallEvaluation","careerSuggestions","resumeImprovementChecklist","learningRoadmap"],"additionalProperties":false,"properties":{"overallEvaluation":{"type":"string","minLength":1,"maxLength":240},"careerSuggestions":{"type":"array","maxItems":1,"items":{"type":"string","maxLength":160}},"resumeImprovementChecklist":{"type":"array","maxItems":1,"items":{"type":"string","maxLength":160}},"learningRoadmap":{"type":"array","maxItems":1,"items":{"type":"string","maxLength":160}}}}',
 TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
