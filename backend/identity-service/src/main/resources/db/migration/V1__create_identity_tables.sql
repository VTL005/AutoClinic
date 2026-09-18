-- =========================================================
-- Identity Service
-- Migration V1: Create identity tables
-- =========================================================


-- =========================================================
-- 1. USERS
-- Quản lý tài khoản Admin, Mechanic và Customer
-- =========================================================

CREATE TABLE users
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    username            VARCHAR(50)  NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(15)  NOT NULL,
    email               VARCHAR(150) NULL,

    role                VARCHAR(20)  NOT NULL,
    account_status      VARCHAR(30)  NOT NULL
                                              DEFAULT 'PENDING_ACTIVATION',

    failed_login_count  INT          NOT NULL DEFAULT 0,
    locked_until        DATETIME(6)  NULL,

    phone_verified_at   DATETIME(6)  NULL,
    email_verified_at   DATETIME(6)  NULL,

    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          DATETIME(6)  NULL,

    created_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),

    version             BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT uk_users_username
        UNIQUE (username),

    CONSTRAINT uk_users_phone
        UNIQUE (phone),

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_role
        CHECK (
            role IN (
                     'ADMIN',
                     'MECHANIC',
                     'CUSTOMER'
                )
            ),

    CONSTRAINT chk_users_account_status
        CHECK (
            account_status IN (
                               'PENDING_ACTIVATION',
                               'ACTIVE',
                               'LOCKED',
                               'DISABLED'
                )
            ),

    CONSTRAINT chk_users_failed_login_count
        CHECK (failed_login_count >= 0),

    CONSTRAINT chk_users_soft_delete
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL)
                OR
            (is_deleted = TRUE)
            )
);

CREATE INDEX idx_users_role
    ON users (role);

CREATE INDEX idx_users_account_status
    ON users (account_status);

CREATE INDEX idx_users_is_deleted
    ON users (is_deleted);

CREATE INDEX idx_users_locked_until
    ON users (locked_until);


-- =========================================================
-- 2. MECHANIC PROFILES
-- Thông tin nghiệp vụ riêng của kỹ thuật viên
-- =========================================================

CREATE TABLE mechanic_profiles
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id             BIGINT         NOT NULL,

    skill_level         VARCHAR(20)    NOT NULL,
    specialization      VARCHAR(100)   NULL,
    hourly_rate         DECIMAL(12, 2) NOT NULL,

    employment_status   VARCHAR(20)    NOT NULL
                                                DEFAULT 'ACTIVE',

    created_at          DATETIME(6)    NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at          DATETIME(6)    NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),

    version             BIGINT         NOT NULL DEFAULT 0,

    CONSTRAINT uk_mechanic_profiles_user_id
        UNIQUE (user_id),

    CONSTRAINT fk_mechanic_profiles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    CONSTRAINT chk_mechanic_skill_level
        CHECK (
            skill_level IN (
                            'JUNIOR',
                            'SENIOR',
                            'EXPERT'
                )
            ),

    CONSTRAINT chk_mechanic_hourly_rate
        CHECK (hourly_rate >= 0),

    CONSTRAINT chk_mechanic_employment_status
        CHECK (
            employment_status IN (
                                  'ACTIVE',
                                  'ON_LEAVE',
                                  'INACTIVE'
                )
            )
);

CREATE INDEX idx_mechanic_profiles_skill_level
    ON mechanic_profiles (skill_level);

CREATE INDEX idx_mechanic_profiles_employment_status
    ON mechanic_profiles (employment_status);


-- =========================================================
-- 3. REFRESH TOKENS
-- Lưu refresh token dưới dạng mã băm
-- Không lưu refresh token gốc
-- =========================================================

CREATE TABLE refresh_tokens
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    user_id             BIGINT       NOT NULL,
    token_hash          VARCHAR(128) NOT NULL,

    expires_at          DATETIME(6)  NOT NULL,
    revoked_at          DATETIME(6)  NULL,

    created_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_refresh_tokens_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,

    CONSTRAINT chk_refresh_tokens_expiration
        CHECK (expires_at > created_at),

    CONSTRAINT chk_refresh_tokens_revocation
        CHECK (
            revoked_at IS NULL
                OR revoked_at >= created_at
            )
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);

CREATE INDEX idx_refresh_tokens_revoked_at
    ON refresh_tokens (revoked_at);