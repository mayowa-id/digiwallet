CREATE TABLE virtual_cards(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    card_number_encrypted VARCHAR(255) NOT NULL,
    card_holder_name VARCHAR(100) NOT NULL,
    cvv_encrypted VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    card_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    currency VARCHAR(3) NOT NULL,
    spending_limit NUMERIC(19, 4),
    spent_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    last_used_at TIMESTAMP,
    card_brand VARCHAR(50),
    external_card_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_card_user ON virtual_cards(user_id);
CREATE INDEX idx_card_wallet ON virtual_cards(wallet_id);
CREATE INDEX idx_card_status ON virtual_cards(card_status);
