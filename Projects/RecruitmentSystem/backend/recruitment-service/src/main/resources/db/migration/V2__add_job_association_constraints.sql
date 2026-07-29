ALTER TABLE job_skills
    ADD CONSTRAINT uk_job_skills_job_skill
        UNIQUE (job_id, skill_id);

ALTER TABLE job_benefits
    ADD CONSTRAINT uk_job_benefits_job_benefit
        UNIQUE (job_id, benefit_id);

CREATE UNIQUE INDEX uk_job_locations_primary_location
    ON job_locations(job_id)
    WHERE primary_location = TRUE;
