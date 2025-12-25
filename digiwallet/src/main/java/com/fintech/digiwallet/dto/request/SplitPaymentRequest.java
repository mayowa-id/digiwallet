package com.fintech.digiwallet.dto.request;


import com.fintech.digiwallet.domain.enums.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitPaymentRequest {

    @NotBlank(message = "Initiator wallet number is required")
    private String initiatorWalletNumber;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotEmpty(message = "Participants list cannot be empty")
    @Valid
    private List<SplitParticipant> participants;

    private String description;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitParticipant {
        @NotBlank(message = "Participant wallet number is required")
        private String walletNumber;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
    }
}