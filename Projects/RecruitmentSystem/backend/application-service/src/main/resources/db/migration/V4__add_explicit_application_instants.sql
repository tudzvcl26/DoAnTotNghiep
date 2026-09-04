-- Preserve legacy zone-less timestamps. Only newly created events populate these
-- explicit instants; historical values require a separately approved migration
-- with deployment-specific timezone provenance.
ALTER TABLE applications
    ADD COLUMN applied_at_instant TIMESTAMP WITH TIME ZONE;

ALTER TABLE application_status_histories
    ADD COLUMN changed_at_instant TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_applications_applied_at_instant
    ON applications (applied_at_instant);

CREATE INDEX idx_status_history_changed_at_instant
    ON application_status_histories (changed_at_instant);
