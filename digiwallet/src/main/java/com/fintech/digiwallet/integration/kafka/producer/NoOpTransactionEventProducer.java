package com.fintech.digiwallet.integration.kafka.producer;

import com.fintech.digiwallet.domain.repository.TransactionEventPublisher;
import com.fintech.digiwallet.dto.event.NotificationEvent;
import com.fintech.digiwallet.dto.event.TransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(
        name = "spring.kafka.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoOpTransactionEventProducer implements TransactionEventPublisher {

    @Override
    public void publishTransactionEvent(TransactionEvent event) {
        log.info("[KAFKA DISABLED] Transaction event ignored: {}",
                event.getTransactionRef());
    }

    @Override
    public void publishNotificationEvent(NotificationEvent event) {
        log.info("[KAFKA DISABLED] Notification event ignored for: {}",
                event.getRecipient());
    }
}
