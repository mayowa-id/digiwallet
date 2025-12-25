package com.fintech.digiwallet.dto.response;


import com.fintech.digiwallet.domain.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private Currency currency;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
}