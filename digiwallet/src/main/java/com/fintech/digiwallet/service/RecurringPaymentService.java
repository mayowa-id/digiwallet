package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.RecurringPayment;
import com.fintech.digiwallet.domain.entity.Transaction;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.PaymentFrequency;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import com.fintech.digiwallet.domain.repository.RecurringPaymentRepository;
import com.fintech.digiwallet.domain.repository.TransactionRepository;
import com.fintech.digiwallet.dto.request.RecurringPaymentRequest;
import com.fintech.digiwallet.service.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final NotificationService notificationService;
    private final TransactionIdGenerator idGenerator;

    @Transactional
    public RecurringPayment createRecurringPayment(RecurringPaymentRequest request) {
        log.info("Creating recurring payment for wallet: {}",
                request.getSourceWalletNumber());

        Wallet sourceWallet = walletService.findWalletByNumber(
                request.getSourceWalletNumber());

        RecurringPayment recurringPayment = RecurringPayment.builder()
                .user(sourceWallet.getUser())
                .sourceWallet(sourceWallet)
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .frequency(request.getFrequency())
                .nextRunDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TransactionStatus.PENDING)
                .executionCount(0)
                .maxExecutions(request.getMaxExecutions())
                .description(request.getDescription())
                .isActive(true)
                .build();

        recurringPayment = recurringPaymentRepository.save(recurringPayment);

        log.info("Recurring payment created: {} - Next run: {}",
                recurringPayment.getId(), recurringPayment.getNextRunDate());

        return recurringPayment;
    }

    @Transactional(readOnly = true)
    public List<RecurringPayment> getUserRecurringPayments(UUID userId) {
        return recurringPaymentRepository.findByUserId(userId);
    }

    @Transactional
    public void cancelRecurringPayment(UUID recurringPaymentId) {
        RecurringPayment payment = recurringPaymentRepository.findById(recurringPaymentId)
                .orElseThrow(() -> new RuntimeException(
                        "Recurring payment not found: " + recurringPaymentId));

        payment.setIsActive(false);
        recurringPaymentRepository.save(payment);

        log.info("Recurring payment cancelled: {}", recurringPaymentId);
    }

    // This runs every minute for demo purposes (in production, use cron: 0 */5 * * * *)
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void processScheduledPayments() {
        log.debug("🔍 Checking for due scheduled payments...");

        LocalDate today = LocalDate.now();
        List<RecurringPayment> duePayments = recurringPaymentRepository
                .findDuePayments(TransactionStatus.PENDING, today);

        if (!duePayments.isEmpty()) {
            log.info("⚡ Found {} due scheduled payment(s)", duePayments.size());
        }

        for (RecurringPayment payment : duePayments) {
            try {
                executeScheduledPayment(payment);
            } catch (Exception e) {
                log.error("Failed to execute scheduled payment: {}",
                        payment.getId(), e);
                payment.setStatus(TransactionStatus.FAILED);
                recurringPaymentRepository.save(payment);
            }
        }
    }

    private void executeScheduledPayment(RecurringPayment payment) {
        log.info("💰 Executing scheduled payment: {} for user: {}",
                payment.getId(), payment.getUser().getEmail());

        Wallet sourceWallet = payment.getSourceWallet();

        // Check if wallet has sufficient funds
        if (sourceWallet.getAvailableBalance().compareTo(payment.getAmount()) < 0) {
            log.warn("Insufficient funds for scheduled payment: {}", payment.getId());
            payment.setStatus(TransactionStatus.FAILED);
            recurringPaymentRepository.save(payment);
            return;
        }

        // Check if max executions reached
        if (payment.getMaxExecutions() != null &&
                payment.getExecutionCount() >= payment.getMaxExecutions()) {
            log.info("Max executions reached for payment: {}", payment.getId());
            payment.setIsActive(false);
            recurringPaymentRepository.save(payment);
            return;
        }

        // Check if end date passed
        if (payment.getEndDate() != null &&
                LocalDate.now().isAfter(payment.getEndDate())) {
            log.info("End date reached for payment: {}", payment.getId());
            payment.setIsActive(false);
            recurringPaymentRepository.save(payment);
            return;
        }

        // Create and execute transaction
        String transactionRef = idGenerator.generateTransactionRef();

        Transaction transaction = Transaction.builder()
                .transactionRef(transactionRef)
                .sourceWallet(sourceWallet)
                .transactionType(TransactionType.PAYMENT)
                .status(TransactionStatus.PROCESSING)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .fee(BigDecimal.ZERO)
                .description("Scheduled Payment: " + payment.getDescription())
                .idempotencyKey("RECURRING-" + payment.getId() + "-" + System.currentTimeMillis())
                .build();

        transaction = transactionRepository.save(transaction);

        // Deduct funds
        walletService.updateBalance(sourceWallet.getId(), payment.getAmount().negate());

        // Create ledger entry
        ledgerService.createWithdrawalLedgerEntry(transaction, sourceWallet);

        // Complete transaction
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        // Update recurring payment
        payment.setExecutionCount(payment.getExecutionCount() + 1);
        payment.setNextRunDate(calculateNextRunDate(payment));
        recurringPaymentRepository.save(payment);

        // Send notification
        String formattedAmount = String.format("%s %s",
                payment.getAmount(), payment.getCurrency());

        notificationService.sendScheduledPaymentNotification(
                payment.getUser().getEmail(),
                transactionRef,
                formattedAmount
        );

        log.info(" Scheduled payment executed successfully: {} - Next run: {}",
                payment.getId(), payment.getNextRunDate());
    }

    private LocalDate calculateNextRunDate(RecurringPayment payment) {
        LocalDate current = payment.getNextRunDate();

        return switch (payment.getFrequency()) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}