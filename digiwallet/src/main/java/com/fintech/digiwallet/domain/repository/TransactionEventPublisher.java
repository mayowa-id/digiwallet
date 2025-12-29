package com.fintech.digiwallet.domain.repository;

import com.fintech.digiwallet.dto.event.NotificationEvent;
import com.fintech.digiwallet.dto.event.TransactionEvent;

public interface TransactionEventPublisher {
    void publishTransactionEvent(TransactionEvent event);
    void publishNotificationEvent(NotificationEvent event);
}
