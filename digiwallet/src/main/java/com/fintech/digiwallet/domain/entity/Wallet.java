package com.fintech.digiwallet.domain.entity;


import com.fintech.digiwallet.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_number", columnList = "wallet_number"),
        @Index(name = "idx_wallet_user", columnList = "user_id"),
        @Index(name = "idx_wallet_currency", columnList = "currency")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_currency", columnNames = {"user_id", "currency"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 20)
    private String walletNumber; // Generated unique wallet number

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false; // One primary wallet per currency

    @OneToMany(mappedBy = "sourceWallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "destinationWallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Transaction> incomingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<LedgerEntry> ledgerEntries = new HashSet<>();
}