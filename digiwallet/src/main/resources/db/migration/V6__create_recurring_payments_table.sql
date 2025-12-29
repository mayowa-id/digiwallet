CREATE TABLE recurring_payments(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    destination_account VARCHAR(100) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    next_run_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    execution_count INTEGER NOT NULL DEFAULT 0,
    max_executions INTEGER,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT check_recurring_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_recurring_user ON recurring_payments(user_id);
CREATE INDEX idx_recurring_wallet ON recurring_payments(source_wallet_id);
CREATE INDEX idx_recurring_next_run ON recurring_payments(next_run_date);
CREATE INDEX idx_recurring_status ON recurring_payments(status);
