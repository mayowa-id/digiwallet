package com.fintech.digiwallet.domain.repository;


import com.fintech.digiwallet.domain.entity.SplitPayment;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SplitPaymentRepository extends JpaRepository<SplitPayment, UUID> {
    List<SplitPayment> findByTransactionId(UUID transactionId);
    List<SplitPayment> findByParticipantWalletId(UUID walletId);
    List<SplitPayment> findByTransactionIdAndStatus(UUID transactionId, TransactionStatus status);
}