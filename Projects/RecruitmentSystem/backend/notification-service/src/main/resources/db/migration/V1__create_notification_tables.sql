CREATE TABLE notifications
(
    id                    UUID PRIMARY KEY,
    event_type            VARCHAR(50)  NOT NULL,
    audience_type         VARCHAR(20)  NOT NULL,
    title                 VARCHAR(200) NOT NULL,
    content               TEXT         NOT NULL,
    payload               JSONB,
    related_resource_type VARCHAR(50),
    related_resource_id   UUID,
    created_by            UUID,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
);

CREATE TABLE notification_user_states
(
    id              UUID PRIMARY KEY,
    notification_id UUID      NOT NULL,
    user_id         UUID      NOT NULL,
    read_at         TIMESTAMP,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,

    CONSTRAINT fk_notification_user_state_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (id),
    CONSTRAINT uq_notification_user_state_notification_user
        UNIQUE (notification_id, user_id)
);

CREATE TABLE notification_preferences
(
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    channel    VARCHAR(20) NOT NULL,
    enabled    BOOLEAN     NOT NULL,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,

    CONSTRAINT uq_notification_preference_user_event_channel
        UNIQUE (user_id, event_type, channel)
);

CREATE TABLE notification_templates
(
    id               UUID PRIMARY KEY,
    code             VARCHAR(100) NOT NULL,
    event_type       VARCHAR(50)  NOT NULL,
    channel          VARCHAR(20)  NOT NULL,
    title_template   VARCHAR(200) NOT NULL,
    content_template TEXT         NOT NULL,
    active           BOOLEAN      NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,

    CONSTRAINT uq_notification_template_code UNIQUE (code),
    CONSTRAINT uq_notification_template_event_channel UNIQUE (event_type, channel)
);

CREATE TABLE notification_delivery_logs
(
    id             UUID PRIMARY KEY,
    notification_id UUID       NOT NULL,
    user_id        UUID        NOT NULL,
    channel        VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL,
    attempt_number INTEGER     NOT NULL,
    attempted_at   TIMESTAMP,
    delivered_at   TIMESTAMP,
    error_message  TEXT,
    created_at     TIMESTAMP   NOT NULL,
    updated_at     TIMESTAMP   NOT NULL,

    CONSTRAINT fk_notification_delivery_log_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (id),
    CONSTRAINT uq_notification_delivery_log_attempt
        UNIQUE (notification_id, user_id, channel, attempt_number)
);

CREATE TABLE notification_event_receipts
(
    id             UUID PRIMARY KEY,
    event_id       UUID         NOT NULL,
    source_service VARCHAR(100) NOT NULL,
    event_type     VARCHAR(50)  NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    payload        JSONB,
    processed_at   TIMESTAMP,
    error_message  TEXT,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,

    CONSTRAINT uq_notification_event_receipt_event UNIQUE (event_id)
);

CREATE INDEX idx_notifications_event_created
    ON notifications (event_type, created_at DESC);
CREATE INDEX idx_notifications_related_resource
    ON notifications (related_resource_type, related_resource_id);
CREATE INDEX idx_notification_user_states_user_created
    ON notification_user_states (user_id, created_at DESC);
CREATE INDEX idx_notification_user_states_unread
    ON notification_user_states (user_id, created_at DESC)
    WHERE read_at IS NULL AND deleted_at IS NULL;
CREATE INDEX idx_notification_delivery_logs_status_attempted
    ON notification_delivery_logs (status, attempted_at);
CREATE INDEX idx_notification_event_receipts_status_created
    ON notification_event_receipts (status, created_at);
