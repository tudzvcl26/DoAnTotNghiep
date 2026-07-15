SET search_path TO user_service;

CREATE TABLE certificates
(
    id UUID PRIMARY KEY,

    profile_id UUID NOT NULL,

    certificate_name VARCHAR(255) NOT NULL,

    issuer_name VARCHAR(255) NOT NULL,

    credential_id VARCHAR(150),

    issue_date DATE NOT NULL,

    expiry_date DATE,

    verification_url VARCHAR(2048),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL,

    CONSTRAINT fk_certificates_profile
        FOREIGN KEY (profile_id)
            REFERENCES profiles(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_certificate_dates
        CHECK (
            expiry_date IS NULL
                OR expiry_date >= issue_date
            )
);

CREATE INDEX idx_certificates_profile_id
    ON certificates(profile_id);

CREATE INDEX idx_certificates_issue_date
    ON certificates(issue_date);

CREATE INDEX idx_certificates_expiry_date
    ON certificates(expiry_date);