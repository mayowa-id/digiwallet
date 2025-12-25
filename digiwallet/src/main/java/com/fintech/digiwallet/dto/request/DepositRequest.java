package com.fintech.digiwallet.dto.request;


import com.fintech.digiwallet.domain.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotBlank(message = "Wallet number is required")
    private String walletNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    private String description;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD, BANK_TRANSFER, etc.

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}