package com.fintech.digiwallet.domain.entity;

import com.fintech.digiwallet.domain.enums.CardStatus;
import com.fintech.digiwallet.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_cards", indexes = {
        @Index(name = "idx_card_user", columnList = "user_id"),
        @Index(name = "idx_card_wallet", columnList = "wallet_id"),
        @Index(name = "idx_card_status", columnList = "card_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualCard extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, length = 255)
    private String cardNumberEncrypted; // Encrypted card number

    @Column(nullable = false, length = 100)
    private String cardHolderName;

    @Column(nullable = false, length = 255)
    private String cvvEncrypted; // Encrypted CVV

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CardStatus cardStatus = CardStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(precision = 19, scale = 4)
    private BigDecimal spendingLimit;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    private LocalDateTime lastUsedAt;

    @Column(length = 50)
    private String cardBrand; // VISA, MASTERCARD, etc.

    @Column(length = 100)
    private String externalCardId; // ID from card issuer provider
}