CREATE TABLE fraud_rules (
    id UUID PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL UNIQUE,
    rule_type VARCHAR(50) NOT NULL,
    rule_config VARCHAR(5000) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INTEGER NOT NULL DEFAULT 0,
    description VARCHAR(1000),
    action VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_fraud_rule_type ON fraud_rules(rule_type);
CREATE INDEX idx_fraud_rule_active ON fraud_rules(is_active);
CREATE INDEX idx_fraud_rule_priority ON fraud_rules(priority);
