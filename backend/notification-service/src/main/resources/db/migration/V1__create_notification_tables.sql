CREATE TABLE notifications (
                               id BIGINT NOT NULL AUTO_INCREMENT,

                               recipient_user_id BIGINT NOT NULL,
                               recipient_email VARCHAR(150) NULL,

                               notification_type VARCHAR(50) NOT NULL,
                               channel VARCHAR(20) NOT NULL,

                               title VARCHAR(200) NOT NULL,
                               content TEXT NOT NULL,

                               reference_type VARCHAR(50) NULL,
                               reference_id BIGINT NULL,
                               source_service VARCHAR(50) NULL,

                               idempotency_key VARCHAR(150) NULL,

                               delivery_status VARCHAR(20) NOT NULL
                                                       DEFAULT 'PENDING',

                               read_at DATETIME(6) NULL,
                               sent_at DATETIME(6) NULL,
                               failed_at DATETIME(6) NULL,
                               failure_reason VARCHAR(500) NULL,

                               created_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

                               updated_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

                               version BIGINT NOT NULL DEFAULT 0,

                               PRIMARY KEY (id),

                               CONSTRAINT uk_notifications_idempotency_key
                                   UNIQUE (idempotency_key),

                               INDEX idx_notifications_recipient_created (
        recipient_user_id,
        created_at
    ),

                               INDEX idx_notifications_recipient_read (
        recipient_user_id,
        read_at
    ),

                               INDEX idx_notifications_delivery_status (
        delivery_status
    ),

                               INDEX idx_notifications_reference (
        reference_type,
        reference_id
    )
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE notification_delivery_attempts (
                                                id BIGINT NOT NULL AUTO_INCREMENT,

                                                notification_id BIGINT NOT NULL,
                                                attempt_number INT NOT NULL,

                                                status VARCHAR(20) NOT NULL,

                                                provider_message_id VARCHAR(200) NULL,
                                                error_message VARCHAR(500) NULL,

                                                attempted_at DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

                                                PRIMARY KEY (id),

                                                CONSTRAINT fk_delivery_attempt_notification
                                                    FOREIGN KEY (notification_id)
                                                        REFERENCES notifications (id)
                                                        ON DELETE CASCADE,

                                                CONSTRAINT uk_delivery_attempt_number
                                                    UNIQUE (
                                                            notification_id,
                                                            attempt_number
                                                        ),

                                                INDEX idx_delivery_attempt_notification (
        notification_id
    )
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;