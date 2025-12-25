package com.fintech.digiwallet.dto.response;


import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private String transactionRef;
    private String sourceWalletNumber;
    private String destinationWalletNumber;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal fee;
    private String description;
    private Map<String, Object> metadata;
    private String failureReason;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}