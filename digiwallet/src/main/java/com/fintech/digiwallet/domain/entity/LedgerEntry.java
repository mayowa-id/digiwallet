package com.fintech.digiwallet.domain.entity;


import com.fintech.digiwallet.domain.enums.AccountType;
import com.fintech.digiwallet.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_transaction", columnList = "transaction_id"),
        @Index(name = "idx_ledger_wallet", columnList = "wallet_id"),
        @Index(name = "idx_ledger_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountType entryType; // DEBIT or CREDIT

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter; // Wallet balance after this entry

    @Column(nullable = false, length = 100)
    private String reference; // Transaction reference for reconciliation

    @Column(length = 500)
    private String description;
}