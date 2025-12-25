package com.fintech.digiwallet.domain.repository;

import com.fintech.digiwallet.domain.entity.Transaction;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import com.fintech.digiwallet.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionRef(String transactionRef);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findBySourceWalletIdOrDestinationWalletId(
            UUID sourceWalletId, UUID destinationWalletId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.sourceWallet.id = :walletId OR t.destinationWallet.id = :walletId) " +
            "AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndStatus(
            @Param("walletId") UUID walletId,
            @Param("status") TransactionStatus status);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "t.sourceWallet.user.id = :userId " +
            "AND t.createdAt >= :startTime")
    long countUserTransactionsSince(
            @Param("userId") UUID userId,
            @Param("startTime") LocalDateTime startTime);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.sourceWallet.id = :walletId OR t.destinationWallet.id = :walletId) " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findWalletTransactionsBetween(
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}