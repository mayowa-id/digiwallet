package com.fintech.digiwallet.domain.repository;


import com.fintech.digiwallet.domain.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {
    List<FraudRule> findByIsActiveTrueOrderByPriorityDesc();
    List<FraudRule> findByRuleTypeAndIsActiveTrue(String ruleType);
}