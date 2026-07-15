CREATE SCHEMA IF NOT EXISTS user_service;

SET search_path TO user_service;

CREATE TABLE profiles
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL UNIQUE,

    display_name VARCHAR(150) NOT NULL,

    headline VARCHAR(255),

    summary TEXT,

    country_code VARCHAR(2),

    province_code VARCHAR(50),

    city_name VARCHAR(120),

    district_name VARCHAR(120),

    contact_email VARCHAR(255),

    contact_phone VARCHAR(30),

    profile_visibility VARCHAR(30) NOT NULL,

    profile_status VARCHAR(30) NOT NULL,

    completion_score INTEGER NOT NULL DEFAULT 0,

    completion_calculated_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_by UUID,

    updated_by UUID,

    deleted_by UUID,

    version BIGINT NOT NULL
);

CREATE INDEX idx_profiles_user_id
    ON profiles(user_id);

CREATE INDEX idx_profiles_visibility
    ON profiles(profile_visibility);

CREATE INDEX idx_profiles_status
    ON profiles(profile_status);