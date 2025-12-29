CREATE TABLE fraud_rules(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    config TEXT,
    enabled BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 0,
    description TEXT,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_fraud_rule_type ON fraud_rules(rule_type);
CREATE INDEX idx_fraud_rule_active ON fraud_rules(enabled);
CREATE INDEX idx_fraud_rule_priority ON fraud_rules(priority);