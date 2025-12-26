package com.fintech.digiwallet.controller;

import com.fintech.digiwallet.domain.entity.RecurringPayment;
import com.fintech.digiwallet.dto.request.RecurringPaymentRequest;
import com.fintech.digiwallet.dto.response.ApiResponse;
import com.fintech.digiwallet.service.RecurringPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-payments")
@RequiredArgsConstructor
@Slf4j
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringPayment>> createRecurringPayment(
            @Valid @RequestBody RecurringPaymentRequest request) {
        log.info("Creating recurring payment for wallet: {}",
                request.getSourceWalletNumber());

        RecurringPayment payment = recurringPaymentService
                .createRecurringPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(payment,
                        "Scheduled payment created successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RecurringPayment>>> getUserRecurringPayments(
            @PathVariable UUID userId) {
        log.info("Fetching recurring payments for user: {}", userId);

        List<RecurringPayment> payments = recurringPaymentService
                .getUserRecurringPayments(userId);

        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> cancelRecurringPayment(
            @PathVariable UUID paymentId) {
        log.info("Cancelling recurring payment: {}", paymentId);

        recurringPaymentService.cancelRecurringPayment(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Scheduled payment cancelled successfully"));
    }
}