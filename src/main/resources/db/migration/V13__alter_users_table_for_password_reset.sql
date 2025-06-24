ALTER TABLE users
    ADD COLUMN password_reset_token VARCHAR(255),
    ADD COLUMN password_reset_token_expires_at TIMESTAMP;

ALTER TABLE users
    DROP COLUMN token,
    DROP COLUMN expires_token_at;