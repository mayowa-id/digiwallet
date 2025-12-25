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
public class TransferRequest {

    @NotBlank(message = "Source wallet number is required")
    private String sourceWalletNumber;

    @NotBlank(message = "Destination wallet number is required")
    private String destinationWalletNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    private String description;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
