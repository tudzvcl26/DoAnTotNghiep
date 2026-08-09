ALTER TABLE notification_event_receipts
    ADD COLUMN event_version INTEGER NOT NULL DEFAULT 1;
