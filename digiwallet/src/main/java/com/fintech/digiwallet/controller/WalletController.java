package com.fintech.digiwallet.controller;

import com.fintech.digiwallet.dto.request.CreateWalletRequest;
import com.fintech.digiwallet.dto.response.ApiResponse;
import com.fintech.digiwallet.dto.response.BalanceResponse;
import com.fintech.digiwallet.dto.response.WalletResponse;
import com.fintech.digiwallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {
        log.info("Creating wallet for user: {}", request.getUserId());

        WalletResponse wallet = walletService.createWallet(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(wallet, "Wallet created successfully"));
    }

    @GetMapping("/{walletNumber}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @PathVariable String walletNumber) {
        log.info("Fetching wallet: {}", walletNumber);

        WalletResponse wallet = walletService.getWalletByNumber(walletNumber);

        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getUserWallets(
            @PathVariable UUID userId) {
        log.info("Fetching wallets for user: {}", userId);

        List<WalletResponse> wallets = walletService.getUserWallets(userId);

        return ResponseEntity.ok(ApiResponse.success(wallets));
    }

    @GetMapping("/{walletNumber}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @PathVariable String walletNumber) {
        log.info("Fetching balance for wallet: {}", walletNumber);

        BalanceResponse balance = walletService.getWalletBalance(walletNumber);

        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}