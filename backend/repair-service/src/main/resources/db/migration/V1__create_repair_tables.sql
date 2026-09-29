CREATE TABLE repair_orders
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    repair_code             VARCHAR(30)  NOT NULL,
    booking_id              BIGINT       NOT NULL,
    customer_user_id        BIGINT       NOT NULL,
    vehicle_id              BIGINT       NOT NULL,
    mechanic_user_id        BIGINT       NULL,
    created_by_user_id      BIGINT       NOT NULL,

    service_type            VARCHAR(200) NOT NULL,
    customer_complaint      VARCHAR(1000) NULL,
    diagnosis               VARCHAR(2000) NULL,
    technician_note         VARCHAR(2000) NULL,

    odometer                INT          NULL,
    status                  VARCHAR(40)  NOT NULL,

    received_at             DATETIME(6)  NOT NULL,
    estimated_completion_at DATETIME(6)  NULL,
    started_at              DATETIME(6)  NULL,
    completed_at            DATETIME(6)  NULL,
    delivered_at            DATETIME(6)  NULL,

    version                 BIGINT       NOT NULL DEFAULT 0,
    created_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                           ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_repair_orders_repair_code
        UNIQUE (repair_code),

    CONSTRAINT uk_repair_orders_booking_id
        UNIQUE (booking_id),

    CONSTRAINT chk_repair_orders_odometer
        CHECK (odometer IS NULL OR odometer >= 0),

    CONSTRAINT chk_repair_orders_status
        CHECK (
            status IN (
                       'RECEIVED',
                       'DIAGNOSING',
                       'WAITING_APPROVAL',
                       'IN_PROGRESS',
                       'COMPLETED',
                       'DELIVERED',
                       'CANCELLED'
                )
            )
);

CREATE INDEX idx_repair_orders_customer_user_id
    ON repair_orders (customer_user_id);

CREATE INDEX idx_repair_orders_vehicle_id
    ON repair_orders (vehicle_id);

CREATE INDEX idx_repair_orders_mechanic_user_id
    ON repair_orders (mechanic_user_id);

CREATE INDEX idx_repair_orders_status
    ON repair_orders (status);

CREATE INDEX idx_repair_orders_received_at
    ON repair_orders (received_at);


CREATE TABLE repair_tasks
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    repair_order_id     BIGINT        NOT NULL,

    task_name           VARCHAR(200)  NOT NULL,
    description         VARCHAR(1000) NULL,
    status              VARCHAR(40)   NOT NULL,

    labor_hours         DECIMAL(6, 2) NULL,
    labor_cost          DECIMAL(15, 2) NOT NULL DEFAULT 0,

    started_at          DATETIME(6)   NULL,
    completed_at        DATETIME(6)   NULL,

    created_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                                         ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_repair_tasks_repair_order
        FOREIGN KEY (repair_order_id)
            REFERENCES repair_orders (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_repair_tasks_status
        CHECK (
            status IN (
                       'PENDING',
                       'IN_PROGRESS',
                       'COMPLETED',
                       'CANCELLED'
                )
            ),

    CONSTRAINT chk_repair_tasks_labor_hours
        CHECK (
            labor_hours IS NULL
                OR labor_hours >= 0
            ),

    CONSTRAINT chk_repair_tasks_labor_cost
        CHECK (labor_cost >= 0)
);

CREATE INDEX idx_repair_tasks_repair_order_id
    ON repair_tasks (repair_order_id);

CREATE INDEX idx_repair_tasks_status
    ON repair_tasks (status);


CREATE TABLE repair_status_history
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    repair_order_id     BIGINT        NOT NULL,
    previous_status     VARCHAR(40)   NULL,
    new_status          VARCHAR(40)   NOT NULL,
    changed_by_user_id  BIGINT        NOT NULL,
    note                VARCHAR(1000) NULL,
    changed_at          DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_repair_status_history_repair_order
        FOREIGN KEY (repair_order_id)
            REFERENCES repair_orders (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_repair_status_history_order_id
    ON repair_status_history (repair_order_id);

CREATE INDEX idx_repair_status_history_changed_at
    ON repair_status_history (changed_at);