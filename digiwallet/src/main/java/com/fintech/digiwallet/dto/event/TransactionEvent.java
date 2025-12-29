package com.fintech.digiwallet.dto.event;

import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class TransactionEvent {
    private UUID transactionId;
    private String transactionRef;
    private UUID sourceWalletId;
    private UUID destinationWalletId;
    private TransactionType transactionType;
    private TransactionStatus status;
    private BigDecimal amount;
    private Currency currency;
    private String sourceUserEmail;
    private String destinationUserEmail;
    private LocalDateTime timestamp;
    private String eventType; // CREATED, COMPLETED, FAILED
}