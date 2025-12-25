package com.fintech.digiwallet.domain.repository;


import com.fintech.digiwallet.domain.entity.Wallet;
import com.fintech.digiwallet.domain.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByWalletNumber(String walletNumber);
    List<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, Currency currency);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.isActive = true")
    List<Wallet> findActiveWalletsByUserId(@Param("userId") UUID userId);

    boolean existsByWalletNumber(String walletNumber);
}