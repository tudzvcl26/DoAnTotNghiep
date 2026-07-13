-- =====================================================
-- AUTH SERVICE SCHEMA
-- Version : V2
-- Description : Authentication & Authorization Tables
-- =====================================================

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(50) NOT NULL UNIQUE,

    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- PERMISSIONS
-- =========================
CREATE TABLE permissions
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(100) NOT NULL UNIQUE,

    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    full_name VARCHAR(150) NOT NULL,

    phone VARCHAR(20),

    avatar_url TEXT,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    verified BOOLEAN NOT NULL DEFAULT FALSE,

    last_login_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- USER ROLES
-- =========================
CREATE TABLE user_roles
(
    user_id UUID NOT NULL,

    role_id UUID NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id)
            ON DELETE CASCADE
);

-- =========================
-- ROLE PERMISSIONS
-- =========================
CREATE TABLE role_permissions
(
    role_id UUID NOT NULL,

    permission_id UUID NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
            REFERENCES permissions(id)
            ON DELETE CASCADE
);

-- =========================
-- REFRESH TOKENS
-- =========================
CREATE TABLE refresh_tokens
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    token TEXT NOT NULL UNIQUE,

    expires_at TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

-- =========================
-- INDEXES
-- =========================
CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_refresh_token_user
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_token_token
    ON refresh_tokens(token);

CREATE INDEX idx_roles_name
    ON roles(name);

CREATE INDEX idx_permissions_name
    ON permissions(name);