package com.fintech.digiwallet.integration.kafka.producer;

import com.fintech.digiwallet.config.KafkaConfig;
import com.fintech.digiwallet.domain.repository.TransactionEventPublisher;
import com.fintech.digiwallet.dto.event.NotificationEvent;
import com.fintech.digiwallet.dto.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaTransactionEventProducer implements TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishTransactionEvent(TransactionEvent event) {
        log.info("Publishing transaction event: {} - {}",
                event.getTransactionRef(), event.getEventType());

        kafkaTemplate.send(
                KafkaConfig.TRANSACTION_EVENTS_TOPIC,
                event.getTransactionRef(),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction event", ex);
            }
        });
    }

    @Override
    public void publishNotificationEvent(NotificationEvent event) {
        log.info("Publishing notification event to: {}", event.getRecipient());

        kafkaTemplate.send(
                KafkaConfig.NOTIFICATION_EVENTS_TOPIC,
                event.getRecipient(),
                event
        );
    }
}
