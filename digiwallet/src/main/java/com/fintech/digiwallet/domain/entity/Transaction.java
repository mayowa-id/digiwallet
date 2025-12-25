package com.fintech.digiwallet.domain.entity;


import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_ref", columnList = "transaction_ref"),
        @Index(name = "idx_transaction_source", columnList = "source_wallet_id"),
        @Index(name = "idx_transaction_dest", columnList = "destination_wallet_id"),
        @Index(name = "idx_transaction_status", columnList = "status"),
        @Index(name = "idx_transaction_created", columnList = "created_at"),
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String transactionRef; // TXN-20241224-XXXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @Column(unique = true, nullable = false, length = 100)
    private String idempotencyKey; // Prevents duplicate transactions

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Column(length = 100)
    private String externalReference; // Reference from payment gateway

    @Column(length = 500)
    private String failureReason;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<LedgerEntry> ledgerEntries = new HashSet<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SplitPayment> splitPayments = new HashSet<>();
}