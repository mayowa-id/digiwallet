package com.fintech.digiwallet.dto.request;


import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.enums.PaymentFrequency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPaymentRequest {

    @NotBlank(message = "Source wallet number is required")
    private String sourceWalletNumber;

    @NotBlank(message = "Destination account is required")
    private String destinationAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Frequency is required")
    private PaymentFrequency frequency;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    private LocalDate endDate;

    private Integer maxExecutions;

    private String description;
}