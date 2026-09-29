CREATE TABLE bookings
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    booking_code VARCHAR(30) NOT NULL,

    customer_user_id BIGINT UNSIGNED NOT NULL,

    vehicle_id BIGINT UNSIGNED NOT NULL,

    mechanic_user_id BIGINT UNSIGNED NULL,

    service_type VARCHAR(100) NOT NULL,

    requested_date DATE NOT NULL,

    requested_time TIME NOT NULL,

    scheduled_start_at DATETIME(6) NULL,

    scheduled_end_at DATETIME(6) NULL,

    customer_note VARCHAR(1000) NULL,

    internal_note VARCHAR(1000) NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    cancellation_reason VARCHAR(500) NULL,

    cancelled_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_bookings_booking_code
        UNIQUE (booking_code),

    CONSTRAINT chk_bookings_status
        CHECK (
            status IN (
                       'PENDING',
                       'CONFIRMED',
                       'IN_PROGRESS',
                       'COMPLETED',
                       'CANCELLED',
                       'REJECTED'
                )
            ),

    CONSTRAINT chk_bookings_schedule
        CHECK (
            scheduled_end_at IS NULL
                OR scheduled_start_at IS NULL
                OR scheduled_end_at > scheduled_start_at
            )
);

CREATE INDEX idx_bookings_customer_user_id
    ON bookings (customer_user_id);

CREATE INDEX idx_bookings_vehicle_id
    ON bookings (vehicle_id);

CREATE INDEX idx_bookings_mechanic_user_id
    ON bookings (mechanic_user_id);

CREATE INDEX idx_bookings_status
    ON bookings (status);

CREATE INDEX idx_bookings_requested_schedule
    ON bookings (
                 requested_date,
                 requested_time
        );

CREATE INDEX idx_bookings_mechanic_schedule
    ON bookings (
                 mechanic_user_id,
                 scheduled_start_at,
                 scheduled_end_at
        );