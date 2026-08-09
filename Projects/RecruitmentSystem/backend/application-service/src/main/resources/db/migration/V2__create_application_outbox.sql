CREATE TABLE application_outbox_events
(
    id            UUID PRIMARY KEY,
    event_id      UUID         NOT NULL UNIQUE,
    aggregate_id  UUID         NOT NULL,
    event_type    VARCHAR(80)  NOT NULL,
    event_version INTEGER      NOT NULL,
    routing_key   VARCHAR(120) NOT NULL,
    payload       JSONB        NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    attempts      INTEGER      NOT NULL DEFAULT 0,
    available_at  TIMESTAMP    NOT NULL,
    published_at  TIMESTAMP,
    last_error    TEXT,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_application_outbox_pending
    ON application_outbox_events(status, available_at, created_at);
