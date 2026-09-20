-- =========================================================
-- Vehicle Service
-- Migration V1: Create vehicle and VIN lookup cache tables
-- =========================================================


-- =========================================================
-- 1. VEHICLES
-- Stores vehicles belonging to customers.
--
-- owner_user_id references a user from Identity Service.
-- It is intentionally NOT a database foreign key because
-- each microservice owns and manages its own database.
-- =========================================================

CREATE TABLE vehicles
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    owner_user_id       BIGINT       NOT NULL,

    vin                 CHAR(17)     NOT NULL,
    license_plate       VARCHAR(20)  NULL,

    manufacturer        VARCHAR(100) NOT NULL,
    model               VARCHAR(100) NOT NULL,
    manufacture_year    SMALLINT UNSIGNED NOT NULL,

    color               VARCHAR(50)  NULL,
    engine_type         VARCHAR(100) NULL,

    fuel_type           VARCHAR(20)  NOT NULL,
    transmission_type   VARCHAR(20)  NULL,

    odometer_km         INT UNSIGNED NOT NULL DEFAULT 0,

    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',

    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          DATETIME(6)  NULL,

    created_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),

    version             BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT uk_vehicles_vin
        UNIQUE (vin),

    CONSTRAINT uk_vehicles_license_plate
        UNIQUE (license_plate),

    CONSTRAINT chk_vehicles_vin_length
        CHECK (CHAR_LENGTH(vin) = 17),

    CONSTRAINT chk_vehicles_manufacture_year
        CHECK (
            manufacture_year >= 1886
                AND manufacture_year <= 2100
            ),

    CONSTRAINT chk_vehicles_fuel_type
        CHECK (
            fuel_type IN (
                          'GASOLINE',
                          'DIESEL',
                          'HYBRID',
                          'ELECTRIC',
                          'OTHER'
                )
            ),

    CONSTRAINT chk_vehicles_transmission_type
        CHECK (
            transmission_type IS NULL
                OR transmission_type IN (
                                         'MANUAL',
                                         'AUTOMATIC',
                                         'CVT',
                                         'DCT',
                                         'OTHER'
                )
            ),

    CONSTRAINT chk_vehicles_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'INACTIVE'
                )
            ),

    CONSTRAINT chk_vehicles_soft_delete
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL)
                OR is_deleted = TRUE
            )
);

CREATE INDEX idx_vehicles_owner_user_id
    ON vehicles (owner_user_id);

CREATE INDEX idx_vehicles_status
    ON vehicles (status);

CREATE INDEX idx_vehicles_is_deleted
    ON vehicles (is_deleted);

CREATE INDEX idx_vehicles_manufacturer_model
    ON vehicles (manufacturer, model);


-- =========================================================
-- 2. VIN LOOKUP CACHE
-- Stores VIN-decoding results returned by an external service.
-- The raw response is retained for troubleshooting and updates.
-- =========================================================

CREATE TABLE vin_lookup_cache
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    vin                 CHAR(17)     NOT NULL,

    manufacturer        VARCHAR(100) NULL,
    model               VARCHAR(100) NULL,
    model_year          SMALLINT UNSIGNED NULL,

    vehicle_type        VARCHAR(100) NULL,
    body_class          VARCHAR(100) NULL,
    engine_description  VARCHAR(255) NULL,
    fuel_type           VARCHAR(100) NULL,

    plant_country       VARCHAR(100) NULL,
    plant_company_name  VARCHAR(150) NULL,

    provider            VARCHAR(50)  NOT NULL,
    raw_response        JSON         NULL,

    fetched_at          DATETIME(6)  NOT NULL,
    expires_at          DATETIME(6)  NOT NULL,

    created_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),

    updated_at          DATETIME(6)  NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6)
                        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT uk_vin_lookup_cache_vin
        UNIQUE (vin),

    CONSTRAINT chk_vin_lookup_cache_vin_length
        CHECK (CHAR_LENGTH(vin) = 17),

    CONSTRAINT chk_vin_lookup_cache_year
        CHECK (
            model_year IS NULL
                OR (
                model_year >= 1886
                    AND model_year <= 2100
                )
            ),

    CONSTRAINT chk_vin_lookup_cache_expiration
        CHECK (expires_at > fetched_at)
);

CREATE INDEX idx_vin_lookup_cache_expires_at
    ON vin_lookup_cache (expires_at);

CREATE INDEX idx_vin_lookup_cache_provider
    ON vin_lookup_cache (provider);