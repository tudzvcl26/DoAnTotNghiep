-- Preserve synchronous routes and the persisted four-category response contract.
UPDATE ai_service.prompt_template_versions SET active = FALSE, updated_at = CURRENT_TIMESTAMP
WHERE template_code = 'INTERVIEW_PREPARATION';
INSERT INTO ai_service.prompt_template_versions
 (id, template_code, version_number, system_prompt, user_prompt_template, output_schema,
  active, created_by, entity_version, created_at, updated_at)
VALUES ('a6000000-0000-0000-0000-000000000002', 'INTERVIEW_PREPARATION', 3,
 'Viết tiếng Việt, giữ nguyên tên công nghệ. Tạo đúng 4 câu hỏi luyện tập: mỗi nhóm 1 câu. Mỗi câu hỏi và dàn ý chỉ 1 câu ngắn. Dàn ý hướng dẫn cách trả lời, không trả lời thay ứng viên. Không bịa dự án, kinh nghiệm hoặc thành tích. Nếu thiếu minh chứng, hỏi có điều kiện và nói rõ chưa có thông tin. Không coi kỹ năng thiếu là kỹ năng đã có. Không lặp lại dữ liệu CV hay bảng điểm.',
 'Chỉ dùng dữ kiện sau làm bối cảnh luyện tập:\n{{context}}',
 '{"type":"object","required":["technicalQuestions","behavioralQuestions","hrQuestions","projectQuestions"],"additionalProperties":false,"properties":{"technicalQuestions":{"$ref":"#/$defs/questions"},"behavioralQuestions":{"$ref":"#/$defs/questions"},"hrQuestions":{"$ref":"#/$defs/questions"},"projectQuestions":{"$ref":"#/$defs/questions"}},"$defs":{"questions":{"type":"array","minItems":1,"maxItems":1,"items":{"type":"object","required":["question","expectedAnswerOutline"],"additionalProperties":false,"properties":{"question":{"type":"string","minLength":1,"maxLength":140},"expectedAnswerOutline":{"type":"string","minLength":1,"maxLength":160}}}}}}',
 TRUE, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
