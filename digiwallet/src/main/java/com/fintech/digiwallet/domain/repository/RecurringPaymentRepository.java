package com.fintech.digiwallet.domain.repository;


import com.fintech.digiwallet.domain.entity.RecurringPayment;
import com.fintech.digiwallet.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, UUID> {
    List<RecurringPayment> findByUserId(UUID userId);

    @Query("SELECT r FROM RecurringPayment r WHERE " +
            "r.isActive = true " +
            "AND r.status = :status " +
            "AND r.nextRunDate <= :date")
    List<RecurringPayment> findDuePayments(
            @Param("status") TransactionStatus status,
            @Param("date") LocalDate date);
}
