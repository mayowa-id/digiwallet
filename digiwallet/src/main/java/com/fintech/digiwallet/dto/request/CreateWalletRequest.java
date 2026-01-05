package com.fintech.digiwallet.dto.request;

import com.fintech.digiwallet.domain.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    private UUID userId;

    @NotNull(message = "Currency is required")
    private Currency currency;
}