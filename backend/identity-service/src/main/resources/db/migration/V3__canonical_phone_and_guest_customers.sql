-- Existing aliases that belong to different users must be reviewed before this migration.
-- The generated column also protects against legacy writers and concurrent registration.
ALTER TABLE users ADD COLUMN is_guest BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN canonical_phone VARCHAR(15) GENERATED ALWAYS AS (
        CASE
            WHEN LEFT(phone,1) = '0' AND CHAR_LENGTH(phone) = 10 THEN CONCAT('+84', SUBSTRING(phone, 2))
            WHEN LEFT(phone,2) = '84' AND CHAR_LENGTH(phone) = 11 THEN CONCAT('+', phone)
            WHEN LEFT(phone,1) BETWEEN '1' AND '9' AND CHAR_LENGTH(phone) BETWEEN 8 AND 14 THEN CONCAT('+', phone)
            ELSE phone
        END
    ) STORED;
CREATE UNIQUE INDEX uk_users_canonical_phone ON users(canonical_phone);
