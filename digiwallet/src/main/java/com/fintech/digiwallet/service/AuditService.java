package com.fintech.digiwallet.service;


import com.fintech.digiwallet.domain.entity.AuditLog;
import com.fintech.digiwallet.domain.entity.User;
import com.fintech.digiwallet.domain.enums.AuditAction;
import com.fintech.digiwallet.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logAction(User user, String entityType, UUID entityId,
                          AuditAction action, Map<String, Object> oldValue,
                          Map<String, Object> newValue) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .entityType(entityType)
                    .entityId(entityId)
                    .actionType(action)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} on {} {}",
                    action, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
            // Don't throw exception - auditing failure shouldn't break business logic
        }
    }
}