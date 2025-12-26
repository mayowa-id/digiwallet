package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.Transaction;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import com.fintech.digiwallet.domain.repository.TransactionRepository;
import com.fintech.digiwallet.dto.request.DepositRequest;
import com.fintech.digiwallet.dto.request.TransferRequest;
import com.fintech.digiwallet.dto.request.WithdrawalRequest;
import com.fintech.digiwallet.dto.response.TransactionResponse;
import com.fintech.digiwallet.dto.mapper.TransactionMapper;
import com.fintech.digiwallet.exception.InsufficientFundsException;
import com.fintech.digiwallet.exception.InvalidTransactionException;
import com.fintech.digiwallet.service.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.01"); // 1% fee

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Processing transfer from {} to {}",
                request.getSourceWalletNumber(), request.getDestinationWalletNumber());

        // Check idempotency
        idempotencyService.checkAndStore(request.getIdempotencyKey());

        try {
            // Validate transfer
            validateTransfer(request);

            // Get wallets
            Wallet sourceWallet = walletService.findWalletByNumber(
                    request.getSourceWalletNumber());
            Wallet destinationWallet = walletService.findWalletByNumber(
                    request.getDestinationWalletNumber());

            // Check if same wallet
            if (sourceWallet.getId().equals(destinationWallet.getId())) {
                throw new InvalidTransactionException(
                        "Cannot transfer to the same wallet");
            }

            // Check currency match
            if (!sourceWallet.getCurrency().equals(destinationWallet.getCurrency())) {
                throw new InvalidTransactionException(
                        "Currency mismatch between wallets");
            }

            // Calculate fee
            BigDecimal fee = calculateFee(request.getAmount());
            BigDecimal totalAmount = request.getAmount().add(fee);

            // Check fraud detection
            fraudDetectionService.checkTransaction(
                    sourceWallet, totalAmount, "TRANSFER");

            // Check sufficient balance
            if (sourceWallet.getAvailableBalance().compareTo(totalAmount) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds. Available: " +
                                sourceWallet.getAvailableBalance());
            }

            // Create transaction
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

            // Reserve funds from source wallet
            walletService.reserveFunds(sourceWallet.getId(), totalAmount);

            // Process transfer
            try {
                // Deduct from source wallet
                walletService.releaseFunds(sourceWallet.getId(), totalAmount);

                // Add to destination wallet
                walletService.updateBalance(destinationWallet.getId(), request.getAmount());

                // Create ledger entries
                ledgerService.createTransferLedgerEntries(
                        transaction, sourceWallet, destinationWallet);

                // Update transaction status
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setCompletedAt(LocalDateTime.now());
                transaction = transactionRepository.save(transaction);

                idempotencyService.markCompleted(
                        request.getIdempotencyKey(),
                        transaction.getTransactionRef());

                log.info("Transfer completed successfully: {}",
                        transaction.getTransactionRef());

                return transactionMapper.toResponse(transaction);

            } catch (Exception e) {
                log.error("Transfer processing failed", e);

                // Rollback: release reserved funds
                walletService.updateBalance(sourceWallet.getId(), totalAmount);

                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason(e.getMessage());
                transactionRepository.save(transaction);

                idempotencyService.markFailed(request.getIdempotencyKey());

                throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            idempotencyService.markFailed(request.getIdempotencyKey());
            throw e;
        }
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        log.info("Processing deposit to wallet: {}", request.getWalletNumber());

        idempotencyService.checkAndStore(request.getIdempotencyKey());

        try {
            Wallet wallet = walletService.findWalletByNumber(request.getWalletNumber());

            // Create transaction
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

            // Add funds to wallet
            walletService.updateBalance(wallet.getId(), request.getAmount());

            // Create ledger entry
            ledgerService.createDepositLedgerEntry(transaction, wallet);

            // Update transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);

            idempotencyService.markCompleted(
                    request.getIdempotencyKey(),
                    transaction.getTransactionRef());

            log.info("Deposit completed: {}", transaction.getTransactionRef());

            return transactionMapper.toResponse(transaction);

        } catch (Exception e) {
            idempotencyService.markFailed(request.getIdempotencyKey());
            throw e;
        }
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest request) {
        log.info("Processing withdrawal from wallet: {}", request.getWalletNumber());

        idempotencyService.checkAndStore(request.getIdempotencyKey());

        try {
            Wallet wallet = walletService.findWalletByNumber(request.getWalletNumber());

            // Check sufficient balance
            if (wallet.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds");
            }

            // Create transaction
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

            // Deduct from wallet
            walletService.updateBalance(wallet.getId(), request.getAmount().negate());

            // Create ledger entry
            ledgerService.createWithdrawalLedgerEntry(transaction, wallet);

            // Update transaction
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);

            idempotencyService.markCompleted(
                    request.getIdempotencyKey(),
                    transaction.getTransactionRef());

            log.info("Withdrawal completed: {}", transaction.getTransactionRef());

            return transactionMapper.toResponse(transaction);

        } catch (Exception e) {
            idempotencyService.markFailed(request.getIdempotencyKey());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByRef(String transactionRef) {
        Transaction transaction = transactionRepository
                .findByTransactionRef(transactionRef)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + transactionRef));

        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getWalletTransactions(
            String walletNumber, Pageable pageable) {

        Wallet wallet = walletService.findWalletByNumber(walletNumber);

        Page<Transaction> transactions = transactionRepository
                .findBySourceWalletIdOrDestinationWalletId(
                        wallet.getId(), wallet.getId(), pageable);

        return transactions.map(transactionMapper::toResponse);
    }

    // Helper methods
    private void validateTransfer(TransferRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_PERCENTAGE)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}