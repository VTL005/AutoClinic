CREATE TABLE account_activation_codes (
    user_id BIGINT NOT NULL PRIMARY KEY,
    code_hash VARCHAR(255) NULL,
    recipient_email VARCHAR(150) NOT NULL,
    expires_at TIMESTAMP(6) NULL,
    consumed_at TIMESTAMP(6) NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    last_issued_at TIMESTAMP(6) NULL,
    send_window_start TIMESTAMP(6) NULL,
    send_count INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_activation_failed_attempts CHECK (failed_attempts >= 0),
    CONSTRAINT chk_activation_send_count CHECK (send_count >= 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
