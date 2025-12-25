package com.fintech.digiwallet.service;

import com.fintech.digiwallet.domain.entity.User;
import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.Currency;
import com.fintech.digiwallet.domain.repository.UserRepository;
import com.fintech.digiwallet.domain.repository.WalletRepository;
import com.fintech.digiwallet.dto.request.CreateWalletRequest;
import com.fintech.digiwallet.dto.response.BalanceResponse;
import com.fintech.digiwallet.dto.response.WalletResponse;
import com.fintech.digiwallet.dto.mapper.WalletMapper;
import com.fintech.digiwallet.domain.exception.UserNotFoundException;
import com.fintech.digiwallet.domain.exception.WalletNotFoundException;
import com.fintech.digiwallet.service.TransactionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final TransactionIdGenerator idGenerator;

    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.info("Creating wallet for user: {} with currency: {}",
                request.getUserId(), request.getCurrency());

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + request.getUserId()));

        // Check if wallet already exists for this currency
        walletRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency())
                .ifPresent(w -> {
                    throw new IllegalStateException(
                            "Wallet already exists for currency: " + request.getCurrency());
                });

        // Create new wallet
        Wallet wallet = Wallet.builder()
                .user(user)
                .walletNumber(idGenerator.generateWalletNumber())
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .pendingBalance(BigDecimal.ZERO)
                .isActive(true)
                .isPrimary(user.getWallets().isEmpty()) // First wallet is primary
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully: {}", savedWallet.getWalletNumber());

        return walletMapper.toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletByNumber(String walletNumber) {
        Wallet wallet = findWalletByNumber(walletNumber);
        return walletMapper.toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getUserWallets(UUID userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return walletMapper.toResponseList(wallets);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getWalletBalance(String walletNumber) {
        Wallet wallet = findWalletByNumber(walletNumber);

        return BalanceResponse.builder()
                .currency(wallet.getCurrency())
                .totalBalance(wallet.getBalance())
                .availableBalance(wallet.getAvailableBalance())
                .pendingBalance(wallet.getPendingBalance())
                .build();
    }

    @Transactional
    public void updateBalance(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with id: " + walletId));

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));

        walletRepository.save(wallet);
        log.debug("Wallet balance updated: {} by {}", walletId, amount);
    }

    @Transactional
    public void reserveFunds(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with id: " + walletId));

        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setPendingBalance(wallet.getPendingBalance().add(amount));

        walletRepository.save(wallet);
        log.debug("Funds reserved: {} from wallet {}", amount, walletId);
    }

    @Transactional
    public void releaseFunds(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with id: " + walletId));

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setPendingBalance(wallet.getPendingBalance().subtract(amount));

        walletRepository.save(wallet);
        log.debug("Funds released: {} from wallet {}", amount, walletId);
    }

    // Helper method
    public Wallet findWalletByNumber(String walletNumber) {
        return walletRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with number: " + walletNumber));
    }
}