package com.fintech.digiwallet.domain.repository;


import com.fintech.digiwallet.domain.entity.VirtualCard;
import com.fintech.digiwallet.domain.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, UUID> {
    List<VirtualCard> findByUserId(UUID userId);
    List<VirtualCard> findByUserIdAndCardStatus(UUID userId, CardStatus status);
    List<VirtualCard> findByWalletId(UUID walletId);
}
