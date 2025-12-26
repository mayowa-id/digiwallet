package com.fintech.digiwallet.integration.kafka.producer;

import com.fintech.digiwallet.config.KafkaConfig;
import com.fintech.digiwallet.dto.event.NotificationEvent;
import com.fintech.digiwallet.dto.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionEvent(TransactionEvent event) {
        log.info("Publishing transaction event: {} - {}",
                event.getTransactionRef(), event.getEventType());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.TRANSACTION_EVENTS_TOPIC,
                event.getTransactionRef(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction event: {}",
                        event.getTransactionRef(), ex);
            } else {
                log.debug("Transaction event published successfully: {}",
                        event.getTransactionRef());
            }
        });
    }

    public void publishNotificationEvent(NotificationEvent event) {
        log.info("Publishing notification event to: {}", event.getRecipient());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaConfig.NOTIFICATION_EVENTS_TOPIC,
                event.getRecipient(),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish notification event", ex);
            } else {
                log.debug("Notification event published successfully");
            }
        });
    }
}