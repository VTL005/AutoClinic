ALTER TABLE payments
    ADD COLUMN provider_order_code VARCHAR(100) NULL
        AFTER payment_method,

    ADD COLUMN checkout_url VARCHAR(1000) NULL
        AFTER provider_order_code,

    ADD COLUMN qr_code TEXT NULL
        AFTER checkout_url,

    ADD COLUMN expires_at DATETIME NULL
        AFTER qr_code,

    ADD COLUMN webhook_received_at DATETIME NULL
        AFTER paid_at,

    ADD COLUMN failure_reason VARCHAR(1000) NULL
        AFTER webhook_received_at;

ALTER TABLE payments
    ADD CONSTRAINT uk_payments_provider_order_code
        UNIQUE (provider_order_code);

CREATE INDEX idx_payments_provider_order_code
    ON payments (provider_order_code);

CREATE INDEX idx_payments_expires_at
    ON payments (expires_at);