package com.fintech.digiwallet.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class TransactionIdGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateTransactionRef() {
        String date = LocalDateTime.now().format(FORMATTER);
        String uniqueId = UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        return "TXN-" + date + "-" + uniqueId;
    }

    public String generateWalletNumber() {
        return "WLT" + System.currentTimeMillis() +
                (int)(Math.random() * 1000);
    }
}