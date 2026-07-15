SET search_path TO user_service;

ALTER TABLE experiences
    RENAME COLUMN company_name TO employer_name;

ALTER TABLE experiences
    RENAME COLUMN position_title TO job_title;

ALTER TABLE experiences
    RENAME COLUMN work_location TO location;