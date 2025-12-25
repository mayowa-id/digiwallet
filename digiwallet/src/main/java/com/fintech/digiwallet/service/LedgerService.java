package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.LedgerEntry;
import com.fintech.digiwallet.domain.entity.Transaction;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.AccountType;
import com.fintech.digiwallet.domain.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public void createTransferLedgerEntries(Transaction transaction,
                                            Wallet sourceWallet,
                                            Wallet destinationWallet) {
        // Debit entry for source wallet
        LedgerEntry debitEntry = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(sourceWallet)
                .entryType(AccountType.DEBIT)
                .amount(transaction.getAmount().add(transaction.getFee()))
                .currency(transaction.getCurrency())
                .balanceAfter(sourceWallet.getBalance())
                .reference(transaction.getTransactionRef())
                .description("Transfer to " + destinationWallet.getWalletNumber())
                .build();

        // Credit entry for destination wallet
        LedgerEntry creditEntry = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(destinationWallet)
                .entryType(AccountType.CREDIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceAfter(destinationWallet.getBalance())
                .reference(transaction.getTransactionRef())
                .description("Transfer from " + sourceWallet.getWalletNumber())
                .build();

        ledgerEntryRepository.saveAll(List.of(debitEntry, creditEntry));
        log.debug("Ledger entries created for transfer: {}", transaction.getTransactionRef());
    }

    @Transactional
    public void createDepositLedgerEntry(Transaction transaction, Wallet wallet) {
        LedgerEntry entry = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(wallet)
                .entryType(AccountType.CREDIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceAfter(wallet.getBalance())
                .reference(transaction.getTransactionRef())
                .description("Deposit")
                .build();

        ledgerEntryRepository.save(entry);
        log.debug("Ledger entry created for deposit: {}", transaction.getTransactionRef());
    }

    @Transactional
    public void createWithdrawalLedgerEntry(Transaction transaction, Wallet wallet) {
        LedgerEntry entry = LedgerEntry.builder()
                .transaction(transaction)
                .wallet(wallet)
                .entryType(AccountType.DEBIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .balanceAfter(wallet.getBalance())
                .reference(transaction.getTransactionRef())
                .description("Withdrawal")
                .build();

        ledgerEntryRepository.save(entry);
        log.debug("Ledger entry created for withdrawal: {}", transaction.getTransactionRef());
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getWalletLedger(UUID walletId) {
        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }
}