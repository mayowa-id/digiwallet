package com.fintech.digiwallet.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "fraud_rules", indexes = {
        @Index(name = "idx_fraud_rule_type", columnList = "rule_type"),
        @Index(name = "idx_fraud_rule_active", columnList = "is_active"),
        @Index(name = "idx_fraud_rule_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRule extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String ruleName;

    @Column(nullable = false, length = 50)
    private String ruleType; // VELOCITY_CHECK, AMOUNT_THRESHOLD, GEO_LOCATION, etc.

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> ruleConfig = new HashMap<>();
    // Example: {"max_transactions_per_hour": 10, "max_amount": 100000}

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0; // Higher number = higher priority

    @Column(length = 1000)
    private String description;

    @Column(length = 50)
    private String action; // BLOCK, REVIEW, ALERT
}