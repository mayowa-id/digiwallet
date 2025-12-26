package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.FraudRule;
import com.fintech.digiwallet.domain.entity.User;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.FraudRiskLevel;
import com.fintech.digiwallet.domain.repository.FraudRuleRepository;
import com.fintech.digiwallet.domain.repository.TransactionRepository;
import com.fintech.digiwallet.exception.FraudDetectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final FraudRuleRepository fraudRuleRepository;
    private final TransactionRepository transactionRepository;

    @Value("${app.fraud.velocity-check-enabled:true}")
    private boolean velocityCheckEnabled;

    @Value("${app.fraud.max-transactions-per-hour:10}")
    private int maxTransactionsPerHour;

    @Value("${app.fraud.suspicious-amount-threshold:100000}")
    private BigDecimal suspiciousAmountThreshold;

    public void checkTransaction(Wallet wallet, BigDecimal amount, String transactionType) {
        log.debug("Running fraud checks for wallet: {} amount: {}",
                wallet.getWalletNumber(), amount);

        FraudRiskLevel riskLevel = assessRiskLevel(wallet, amount, transactionType);

        if (riskLevel == FraudRiskLevel.CRITICAL || riskLevel == FraudRiskLevel.HIGH) {
            log.warn("High fraud risk detected for wallet: {} level: {}",
                    wallet.getWalletNumber(), riskLevel);

            // In production, this might queue for manual review instead of blocking
            throw new FraudDetectedException(
                    "Transaction blocked due to fraud detection. Contact support.");
        }

        if (riskLevel == FraudRiskLevel.MEDIUM) {
            log.info("Medium fraud risk detected for wallet: {}", wallet.getWalletNumber());
            // In production, might add additional verification steps
        }
    }

    private FraudRiskLevel assessRiskLevel(Wallet wallet, BigDecimal amount, String transactionType) {
        List<FraudRule> activeRules = fraudRuleRepository.findByIsActiveTrueOrderByPriorityDesc();

        FraudRiskLevel maxRiskLevel = FraudRiskLevel.LOW;

        for (FraudRule rule : activeRules) {
            FraudRiskLevel riskLevel = evaluateRule(rule, wallet, amount, transactionType);

            if (riskLevel.ordinal() > maxRiskLevel.ordinal()) {
                maxRiskLevel = riskLevel;
            }

            // If critical risk, stop checking other rules
            if (maxRiskLevel == FraudRiskLevel.CRITICAL) {
                break;
            }
        }

        return maxRiskLevel;
    }

    private FraudRiskLevel evaluateRule(FraudRule rule, Wallet wallet,
                                        BigDecimal amount, String transactionType) {
        try {
            switch (rule.getRuleType()) {
                case "VELOCITY_CHECK":
                    return checkVelocity(wallet.getUser(), rule.getRuleConfig());

                case "AMOUNT_THRESHOLD":
                    return checkAmountThreshold(amount, rule.getRuleConfig());

                case "DAILY_LIMIT":
                    return checkDailyLimit(wallet, amount, rule.getRuleConfig());

                default:
                    return FraudRiskLevel.LOW;
            }
        } catch (Exception e) {
            log.error("Error evaluating fraud rule: {}", rule.getRuleName(), e);
            return FraudRiskLevel.LOW;
        }
    }

    private FraudRiskLevel checkVelocity(User user, Map<String, Object> config) {
        if (!velocityCheckEnabled) {
            return FraudRiskLevel.LOW;
        }

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTransactions = transactionRepository
                .countUserTransactionsSince(user.getId(), oneHourAgo);

        Integer maxTransactions = config.get("max_transactions_per_hour") != null
                ? ((Number) config.get("max_transactions_per_hour")).intValue()
                : maxTransactionsPerHour;

        if (recentTransactions >= maxTransactions * 2) {
            return FraudRiskLevel.CRITICAL;
        } else if (recentTransactions >= maxTransactions) {
            return FraudRiskLevel.HIGH;
        } else if (recentTransactions >= maxTransactions * 0.75) {
            return FraudRiskLevel.MEDIUM;
        }

        return FraudRiskLevel.LOW;
    }

    private FraudRiskLevel checkAmountThreshold(BigDecimal amount, Map<String, Object> config) {
        BigDecimal threshold = config.get("threshold_amount") != null
                ? new BigDecimal(config.get("threshold_amount").toString())
                : suspiciousAmountThreshold;

        if (amount.compareTo(threshold.multiply(new BigDecimal("2"))) >= 0) {
            return FraudRiskLevel.CRITICAL;
        } else if (amount.compareTo(threshold) >= 0) {
            return FraudRiskLevel.HIGH;
        } else if (amount.compareTo(threshold.multiply(new BigDecimal("0.75"))) >= 0) {
            return FraudRiskLevel.MEDIUM;
        }

        return FraudRiskLevel.LOW;
    }

    private FraudRiskLevel checkDailyLimit(Wallet wallet, BigDecimal amount,
                                           Map<String, Object> config) {
        // This would check total transactions for the day
        // For now, returning LOW
        return FraudRiskLevel.LOW;
    }
}