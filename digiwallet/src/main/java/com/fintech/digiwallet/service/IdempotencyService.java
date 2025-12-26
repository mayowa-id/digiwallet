package com.fintech.digiwallet.service;


import com.fintech.digiwallet.exception.DuplicateTransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final Duration EXPIRATION = Duration.ofHours(24);

    public void checkAndStore(String idempotencyKey) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;

        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "processing", EXPIRATION);

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate transaction attempt with idempotency key: {}", idempotencyKey);
            throw new DuplicateTransactionException(
                    "A transaction with this idempotency key already exists");
        }
    }

    public void markCompleted(String idempotencyKey, String transactionRef) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, transactionRef, EXPIRATION);
    }

    public void markFailed(String idempotencyKey) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        redisTemplate.delete(key);
    }
}