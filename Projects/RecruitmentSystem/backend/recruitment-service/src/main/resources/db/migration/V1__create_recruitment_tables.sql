CREATE TABLE job_categories
(
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon VARCHAR(255),

    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    parent_id UUID,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_job_category_parent
        FOREIGN KEY (parent_id)
            REFERENCES job_categories(id)
);

CREATE TABLE skills
(
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon VARCHAR(255),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE benefits
(
    id UUID PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon VARCHAR(255),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE jobs
(
    id UUID PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    job_code VARCHAR(50) NOT NULL UNIQUE,

    description TEXT,
    requirements TEXT,
    responsibilities TEXT,

    salary_min NUMERIC(15,2),
    salary_max NUMERIC(15,2),

    currency VARCHAR(10),

    employment_type VARCHAR(50) NOT NULL,
    experience_level VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    quantity INTEGER NOT NULL,

    application_deadline DATE,

    published_at TIMESTAMP,
    expired_at TIMESTAMP,

    remote_allowed BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,

    company_id UUID NOT NULL,

    category_id UUID,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_job_category
        FOREIGN KEY (category_id)
            REFERENCES job_categories(id)
);

CREATE TABLE job_skills
(
    id UUID PRIMARY KEY,

    job_id UUID NOT NULL,
    skill_id UUID NOT NULL,

    skill_level VARCHAR(50) NOT NULL,

    required BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_job_skill_job
        FOREIGN KEY (job_id)
            REFERENCES jobs(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_job_skill_skill
        FOREIGN KEY (skill_id)
            REFERENCES skills(id)
);

CREATE TABLE job_benefits
(
    id UUID PRIMARY KEY,

    job_id UUID NOT NULL,
    benefit_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_job_benefit_job
        FOREIGN KEY (job_id)
            REFERENCES jobs(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_job_benefit_benefit
        FOREIGN KEY (benefit_id)
            REFERENCES benefits(id)
);

CREATE TABLE job_locations
(
    id UUID PRIMARY KEY,

    job_id UUID NOT NULL,

    province VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    address VARCHAR(255),

    primary_location BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_job_location_job
        FOREIGN KEY (job_id)
            REFERENCES jobs(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_job_company
    ON jobs(company_id);

CREATE INDEX idx_job_category
    ON jobs(category_id);

CREATE INDEX idx_job_status
    ON jobs(status);

CREATE INDEX idx_job_deadline
    ON jobs(application_deadline);

CREATE INDEX idx_job_skill_job
    ON job_skills(job_id);

CREATE INDEX idx_job_skill_skill
    ON job_skills(skill_id);

CREATE INDEX idx_job_benefit_job
    ON job_benefits(job_id);

CREATE INDEX idx_job_location_job
    ON job_locations(job_id);