package com.fintech.digiwallet.domain.repository;


import com.fintech.digiwallet.domain.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
    List<LedgerEntry> findByTransactionId(UUID transactionId);

    @Query("SELECT l FROM LedgerEntry l WHERE " +
            "l.wallet.id = :walletId " +
            "AND l.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY l.createdAt DESC")
    List<LedgerEntry> findWalletLedgerEntriesBetween(
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
