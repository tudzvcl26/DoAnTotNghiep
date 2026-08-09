SET search_path TO ai_service;

ALTER TABLE resume_documents
    ADD COLUMN deleted_at TIMESTAMP;

CREATE INDEX idx_resume_documents_owner_active
    ON resume_documents(owner_user_id, upload_time DESC)
    WHERE deleted_at IS NULL;
