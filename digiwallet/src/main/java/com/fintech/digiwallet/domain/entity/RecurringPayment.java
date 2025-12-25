package com.fintech.digiwallet.domain.entity;


import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.PaymentFrequency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_payments", indexes = {
        @Index(name = "idx_recurring_user", columnList = "user_id"),
        @Index(name = "idx_recurring_wallet", columnList = "source_wallet_id"),
        @Index(name = "idx_recurring_next_run", columnList = "next_run_date"),
        @Index(name = "idx_recurring_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id", nullable = false)
    private Wallet sourceWallet;

    @Column(nullable = false, length = 100)
    private String destinationAccount; // Wallet number or external account

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentFrequency frequency;

    @Column(nullable = false)
    private LocalDate nextRunDate;

    private LocalDate endDate; // Optional end date

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Integer executionCount = 0;

    private Integer maxExecutions; // Optional max number of executions

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}