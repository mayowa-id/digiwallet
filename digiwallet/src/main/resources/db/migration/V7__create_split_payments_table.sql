CREATE TABLE split_payments (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    participant_wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    settled_at TIMESTAMP,
    settlement_transaction_ref VARCHAR(100),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT check_split_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_split_transaction ON split_payments(transaction_id);
CREATE INDEX idx_split_participant ON split_payments(participant_wallet_id);
CREATE INDEX idx_split_status ON split_payments(status);
