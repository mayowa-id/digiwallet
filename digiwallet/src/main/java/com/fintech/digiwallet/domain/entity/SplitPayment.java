package com.fintech.digiwallet.domain.entity;

import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "split_payments", indexes = {
        @Index(name = "idx_split_transaction", columnList = "transaction_id"),
        @Index(name = "idx_split_participant", columnList = "participant_wallet_id"),
        @Index(name = "idx_split_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction; // Main transaction

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_wallet_id", nullable = false)
    private Wallet participantWallet;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount; // Amount this participant needs to pay

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    private LocalDateTime settledAt;

    @Column(length = 100)
    private String settlementTransactionRef; // Reference to the settlement transaction

    @Column(length = 500)
    private String description;
}