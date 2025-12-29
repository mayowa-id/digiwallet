CREATE TABLE transactions(
    id UUID PRIMARY KEY,
    transaction_ref VARCHAR(50) NOT NULL UNIQUE,
    source_wallet_id UUID REFERENCES wallets(id),
    destination_wallet_id UUID REFERENCES wallets(id),
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    fee NUMERIC(19, 4) NOT NULL DEFAULT 0,
    description VARCHAR(500),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    metadata VARCHAR(5000),
    external_reference VARCHAR(100),
    failure_reason VARCHAR(500),
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT check_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transaction_ref ON transactions(transaction_ref);
CREATE INDEX idx_transaction_source ON transactions(source_wallet_id);
CREATE INDEX idx_transaction_dest ON transactions(destination_wallet_id);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_created ON transactions(created_at);
CREATE INDEX idx_idempotency_key ON transactions(idempotency_key);
