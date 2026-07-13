-- =====================================================
-- Version : V3
-- Description : Add updated_at column to refresh_tokens
-- =====================================================

ALTER TABLE refresh_tokens
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;