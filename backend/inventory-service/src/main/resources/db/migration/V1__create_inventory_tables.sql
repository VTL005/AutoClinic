CREATE TABLE parts
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_code         VARCHAR(50)     NOT NULL,
    name              VARCHAR(200)    NOT NULL,
    description       VARCHAR(1000)   NULL,
    manufacturer      VARCHAR(150)    NULL,
    unit              VARCHAR(30)     NOT NULL,
    unit_price        DECIMAL(15, 2)  NOT NULL,
    quantity_in_stock INT             NOT NULL DEFAULT 0,
    minimum_stock     INT             NOT NULL DEFAULT 0,
    active            BOOLEAN         NOT NULL DEFAULT TRUE,
    version           BIGINT          NOT NULL DEFAULT 0,
    created_at        DATETIME(6)     NOT NULL,
    updated_at        DATETIME(6)     NOT NULL,

    CONSTRAINT uk_parts_part_code
        UNIQUE (part_code),

    CONSTRAINT chk_parts_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_parts_quantity
        CHECK (quantity_in_stock >= 0),

    CONSTRAINT chk_parts_minimum_stock
        CHECK (minimum_stock >= 0)
);

CREATE TABLE inventory_transactions
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_id              BIGINT        NOT NULL,
    transaction_type     VARCHAR(30)   NOT NULL,
    quantity             INT           NOT NULL,
    quantity_before      INT           NOT NULL,
    quantity_after       INT           NOT NULL,
    reference_type       VARCHAR(50)   NULL,
    reference_id         BIGINT        NULL,
    note                 VARCHAR(1000) NULL,
    performed_by_user_id BIGINT        NOT NULL,
    created_at           DATETIME(6)   NOT NULL,

    CONSTRAINT fk_inventory_transactions_part
        FOREIGN KEY (part_id)
            REFERENCES parts (id),

    CONSTRAINT chk_inventory_transaction_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_inventory_quantity_before
        CHECK (quantity_before >= 0),

    CONSTRAINT chk_inventory_quantity_after
        CHECK (quantity_after >= 0)
);

CREATE INDEX idx_parts_name
    ON parts (name);

CREATE INDEX idx_parts_active
    ON parts (active);

CREATE INDEX idx_inventory_transactions_part
    ON inventory_transactions (part_id);

CREATE INDEX idx_inventory_transactions_created_at
    ON inventory_transactions (created_at);