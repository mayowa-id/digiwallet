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
public class VirtualCardRequest {

    @NotBlank(message = "Wallet number is required")
    private String walletNumber;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @DecimalMin(value = "0.01", message = "Spending limit must be greater than 0")
    private BigDecimal spendingLimit;
}
