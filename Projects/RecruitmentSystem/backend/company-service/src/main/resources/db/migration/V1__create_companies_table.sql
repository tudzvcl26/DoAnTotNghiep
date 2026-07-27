CREATE TABLE companies
(
    id UUID PRIMARY KEY,

    owner_id UUID NOT NULL,

    name VARCHAR(255) NOT NULL,

    slug VARCHAR(255) NOT NULL,

    description TEXT,

    website VARCHAR(255),

    email VARCHAR(255),

    phone VARCHAR(50),

    tax_code VARCHAR(100),

    company_type VARCHAR(50) NOT NULL,

    company_size VARCHAR(50) NOT NULL,

    verification_status VARCHAR(50) NOT NULL,

    status VARCHAR(50) NOT NULL,

    logo_url VARCHAR(500),

    banner_url VARCHAR(500),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE companies
    ADD CONSTRAINT uk_company_slug
        UNIQUE (slug);

ALTER TABLE companies
    ADD CONSTRAINT uk_company_tax_code
        UNIQUE (tax_code);

CREATE INDEX idx_company_owner
    ON companies(owner_id);

CREATE INDEX idx_company_status
    ON companies(status);

CREATE INDEX idx_company_verification_status
    ON companies(verification_status);