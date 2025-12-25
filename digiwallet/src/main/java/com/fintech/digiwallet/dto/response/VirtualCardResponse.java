package com.fintech.digiwallet.dto.response;



import com.fintech.digiwallet.domain.enums.CardStatus;
import com.fintech.digiwallet.domain.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCardResponse {
    private UUID id;
    private String maskedCardNumber; // Only last 4 digits
    private String cardHolderName;
    private LocalDate expiryDate;
    private CardStatus cardStatus;
    private Currency currency;
    private BigDecimal spendingLimit;
    private BigDecimal spentAmount;
    private BigDecimal availableLimit;
    private String cardBrand;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
