CREATE TABLE wallets(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_number VARCHAR(20) NOT NULL UNIQUE,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    available_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    pending_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_currency UNIQUE (user_id, currency),
    CONSTRAINT check_balance_positive CHECK (balance >= 0),
    CONSTRAINT check_available_balance_positive CHECK (available_balance >= 0)
);

CREATE INDEX idx_wallet_number ON wallets(wallet_number);
CREATE INDEX idx_wallet_user ON wallets(user_id);
CREATE INDEX idx_wallet_currency ON wallets(currency);