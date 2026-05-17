CREATE TABLE IF NOT EXISTS mercado_livre_config (
    id BIGSERIAL PRIMARY KEY,
    access_token VARCHAR(600) NOT NULL,
    refresh_token VARCHAR(200) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    seller_id VARCHAR(50) NOT NULL DEFAULT '1635399587'
);