package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.Transaction;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import com.fintech.digiwallet.domain.repository.TransactionEventPublisher;
import com.fintech.digiwallet.domain.repository.TransactionRepository;
import com.fintech.digiwallet.dto.event.TransactionEvent;
import com.fintech.digiwallet.dto.mapper.TransactionMapper;
import com.fintech.digiwallet.dto.request.DepositRequest;
import com.fintech.digiwallet.dto.request.TransferRequest;
import com.fintech.digiwallet.dto.request.WithdrawalRequest;
import com.fintech.digiwallet.dto.response.TransactionResponse;
import com.fintech.digiwallet.exception.InsufficientFundsException;
import com.fintech.digiwallet.exception.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final IdempotencyService idempotencyService;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionMapper transactionMapper;
    private final TransactionIdGenerator idGenerator;

    // ✅ Interface ONLY (Kafka-safe)
    private final TransactionEventPublisher eventPublisher;

    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.01"); // 1% fee

    // ===================== TRANSFER =====================
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Processing transfer from {} to {}",
                request.getSourceWalletNumber(), request.getDestinationWalletNumber());

        idempotencyService.checkAndStore(request.getIdempotencyKey());

        try {
            validateTransfer(request);

            Wallet sourceWallet = walletService.findWalletByNumber(
                    request.getSourceWalletNumber());
            Wallet destinationWallet = walletService.findWalletByNumber(
                    request.getDestinationWalletNumber());

            if (sourceWallet.getId().equals(destinationWallet.getId())) {
                throw new InvalidTransactionException("Cannot transfer to the same wallet");
            }

            if (!sourceWallet.getCurrency().equals(destinationWallet.getCurrency())) {
                throw new InvalidTransactionException("Currency mismatch between wallets");
            }

            BigDecimal fee = calculateFee(request.getAmount());
            BigDecimal totalAmount = request.getAmount().add(fee);

            fraudDetectionService.checkTransaction(
                    sourceWallet, totalAmount, "TRANSFER");

            if (sourceWallet.getAvailableBalance().compareTo(totalAmount) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds. Available: " +
                                sourceWallet.getAvailableBalance());
            }

            Transaction transaction = Transaction.builder()
                    .transactionRef(idGenerator.generateTransactionRef())
                    .sourceWallet(sourceWallet)
                    .destinationWallet(destinationWallet)
                    .transactionType(TransactionType.TRANSFER)
                    .status(TransactionStatus.PROCESSING)
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .fee(fee)
                    .description(request.getDescription())
                    .idempotencyKey(request.getIdempotencyKey())
                    .metadata(new HashMap<>())
                    .build();

            transaction = transactionRepository.save(transaction);

            walletService.reserveFunds(sourceWallet.getId(), totalAmount);

            try {
                walletService.releaseFunds(sourceWallet.getId(), totalAmount);
                walletService.updateBalance(destinationWallet.getId(), request.getAmount());

                ledgerService.createTransferLedgerEntries(
                        transaction, sourceWallet, destinationWallet);

                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transaction = transactionRepository.save(transaction);

                publishTransactionCompletedEvent(transaction);

                idempotencyService.markCompleted(
                        request.getIdempotencyKey(),
                        transaction.getTransactionRef());

                return transactionMapper.toResponse(transaction);

            } catch (Exception e) {
                walletService.updateBalance(sourceWallet.getId(), totalAmount);

                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason(e.getMessage());
                transactionRepository.save(transaction);

                idempotencyService.markFailed(request.getIdempotencyKey());
                throw new RuntimeException("Transfer failed", e);
            }

        } catch (Exception e) {
            idempotencyService.markFailed(request.getIdempotencyKey());
            throw e;
        }
    }

    // ===================== DEPOSIT =====================
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        log.info("Processing deposit to wallet: {}", request.getWalletNumber());

        idempotencyService.checkAndStore(request.getIdempotencyKey());

        Wallet wallet = walletService.findWalletByNumber(request.getWalletNumber());

        Transaction transaction = Transaction.builder()
                .transactionRef(idGenerator.generateTransactionRef())
                .destinationWallet(wallet)
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.PROCESSING)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fee(BigDecimal.ZERO)
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .metadata(Map.of("payment_method", request.getPaymentMethod()))
                .build();

        transaction = transactionRepository.save(transaction);

        walletService.updateBalance(wallet.getId(), request.getAmount());
        ledgerService.createDepositLedgerEntry(transaction, wallet);

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        idempotencyService.markCompleted(
                request.getIdempotencyKey(),
                transaction.getTransactionRef());

        return transactionMapper.toResponse(transaction);
    }

    // ===================== WITHDRAW =====================
    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest request) {
        log.info("Processing withdrawal from wallet: {}", request.getWalletNumber());

        idempotencyService.checkAndStore(request.getIdempotencyKey());

        Wallet wallet = walletService.findWalletByNumber(request.getWalletNumber());

        if (wallet.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        Transaction transaction = Transaction.builder()
                .transactionRef(idGenerator.generateTransactionRef())
                .sourceWallet(wallet)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PROCESSING)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fee(BigDecimal.ZERO)
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .metadata(Map.of("destination", request.getDestinationAccount()))
                .build();

        transaction = transactionRepository.save(transaction);

        walletService.updateBalance(wallet.getId(), request.getAmount().negate());
        ledgerService.createWithdrawalLedgerEntry(transaction, wallet);

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        idempotencyService.markCompleted(
                request.getIdempotencyKey(),
                transaction.getTransactionRef());

        return transactionMapper.toResponse(transaction);
    }

    // ===================== QUERIES =====================
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByRef(String transactionRef) {
        return transactionRepository.findByTransactionRef(transactionRef)
                .map(transactionMapper::toResponse)
                .orElseThrow(() ->
                        new RuntimeException("Transaction not found: " + transactionRef));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getWalletTransactions(
            String walletNumber, Pageable pageable) {

        Wallet wallet = walletService.findWalletByNumber(walletNumber);

        return transactionRepository
                .findBySourceWalletIdOrDestinationWalletId(
                        wallet.getId(), wallet.getId(), pageable)
                .map(transactionMapper::toResponse);
    }

    // ===================== HELPERS =====================
    private void validateTransfer(TransferRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_PERCENTAGE)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void publishTransactionCompletedEvent(Transaction transaction) {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(transaction.getId())
                .transactionRef(transaction.getTransactionRef())
                .sourceWalletId(transaction.getSourceWallet() != null ?
                        transaction.getSourceWallet().getId() : null)
                .destinationWalletId(transaction.getDestinationWallet() != null ?
                        transaction.getDestinationWallet().getId() : null)
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .timestamp(LocalDateTime.now())
                .eventType("COMPLETED")
                .build();

        // ✅ Interface call (Kafka ON/OFF safe)
        eventPublisher.publishTransactionEvent(event);
    }
}
