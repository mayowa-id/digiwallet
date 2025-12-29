package com.fintech.digiwallet.domain.entity;


import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.PaymentFrequency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "RECURRING_PAYMENTS", indexes = {
        @Index(name = "IDX_RECURRING_USER", columnList = "USER_ID"),
        @Index(name = "IDX_RECURRING_WALLET", columnList = "SOURCE_WALLET_ID"),
        @Index(name = "IDX_RECURRING_NEXT_RUN", columnList = "NEXT_RUN_DATE"),
        @Index(name = "IDX_RECURRING_STATUS", columnList = "STATUS")
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