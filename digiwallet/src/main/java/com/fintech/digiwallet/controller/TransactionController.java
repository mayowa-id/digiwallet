package com.fintech.digiwallet.controller;

import com.fintech.digiwallet.dto.request.DepositRequest;
import com.fintech.digiwallet.dto.request.TransferRequest;
import com.fintech.digiwallet.dto.request.WithdrawalRequest;
import com.fintech.digiwallet.dto.response.ApiResponse;
import com.fintech.digiwallet.dto.response.TransactionResponse;
import com.fintech.digiwallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request) {
        log.info("Transfer request from {} to {}",
                request.getSourceWalletNumber(), request.getDestinationWalletNumber());

        TransactionResponse transaction = transactionService.transfer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "Transfer initiated successfully"));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositRequest request) {
        log.info("Deposit request to wallet: {}", request.getWalletNumber());

        TransactionResponse transaction = transactionService.deposit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "Deposit completed successfully"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawalRequest request) {
        log.info("Withdrawal request from wallet: {}", request.getWalletNumber());

        TransactionResponse transaction = transactionService.withdraw(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "Withdrawal completed successfully"));
    }

    @GetMapping("/{transactionRef}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String transactionRef) {
        log.info("Fetching transaction: {}", transactionRef);

        TransactionResponse transaction = transactionService
                .getTransactionByRef(transactionRef);

        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @GetMapping("/wallet/{walletNumber}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getWalletTransactions(
            @PathVariable String walletNumber,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("Fetching transactions for wallet: {}", walletNumber);

        Page<TransactionResponse> transactions = transactionService
                .getWalletTransactions(walletNumber, pageable);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}