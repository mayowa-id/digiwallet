package com.fintech.digiwallet.service;

import com.fintech.digiwallet.dto.event.NotificationEvent;
import com.fintech.digiwallet.dto.event.TransactionEvent;
import com.fintech.digiwallet.integration.kafka.producer.TransactionEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final TransactionEventProducer eventProducer;

    @KafkaListener(
            topics = "transaction-events",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event: {} - {}",
                event.getTransactionRef(), event.getEventType());

        if ("COMPLETED".equals(event.getEventType())) {
            sendTransactionNotifications(event);
        }
    }

    private void sendTransactionNotifications(TransactionEvent event) {
        // Send notification to source user (if exists)
        if (event.getSourceUserEmail() != null) {
            NotificationEvent notification = NotificationEvent.builder()
                    .recipient(event.getSourceUserEmail())
                    .notificationType("EMAIL")
                    .subject("Transaction Completed")
                    .message(buildDebitMessage(event))
                    .templateData(buildTemplateData(event))
                    .timestamp(event.getTimestamp())
                    .build();

            sendNotification(notification);
        }

        // Send notification to destination user (if exists)
        if (event.getDestinationUserEmail() != null) {
            NotificationEvent notification = NotificationEvent.builder()
                    .recipient(event.getDestinationUserEmail())
                    .notificationType("EMAIL")
                    .subject("Funds Received")
                    .message(buildCreditMessage(event))
                    .templateData(buildTemplateData(event))
                    .timestamp(event.getTimestamp())
                    .build();

            sendNotification(notification);
        }
    }

    public void sendScheduledPaymentNotification(String userEmail,
                                                 String transactionRef,
                                                 String amount) {
        log.info("🔔 SENDING SCHEDULED PAYMENT NOTIFICATION to: {}", userEmail);

        String message = String.format(
                "✅ Your scheduled payment of %s has been processed successfully! " +
                        "Transaction Reference: %s",
                amount, transactionRef
        );

        NotificationEvent notification = NotificationEvent.builder()
                .recipient(userEmail)
                .notificationType("EMAIL")
                .subject("💰 Scheduled Payment Processed")
                .message(message)
                .templateData(Map.of(
                        "type", "SCHEDULED_PAYMENT",
                        "transactionRef", transactionRef,
                        "amount", amount
                ))
                .timestamp(java.time.LocalDateTime.now())
                .build();

        sendNotification(notification);
    }

    private void sendNotification(NotificationEvent notification) {
        // Publish to Kafka for async processing
        eventProducer.publishNotificationEvent(notification);

        // For demo purposes, also log to console with emoji
        log.info("📧 NOTIFICATION SENT to {}: {}",
                notification.getRecipient(),
                notification.getMessage());
    }

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-sender-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processNotification(NotificationEvent event) {
        log.info("📬 Processing notification for: {}", event.getRecipient());

        // In production, this would send actual emails/SMS
        // For now, we'll just log with fancy formatting
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔔 NEW NOTIFICATION");
        System.out.println("=".repeat(80));
        System.out.println("📧 To: " + event.getRecipient());
        System.out.println("📌 Subject: " + event.getSubject());
        System.out.println("💬 Message: " + event.getMessage());
        System.out.println("⏰ Time: " + event.getTimestamp());
        System.out.println("=".repeat(80) + "\n");
    }

    private String buildDebitMessage(TransactionEvent event) {
        return String.format(
                "Your %s transaction of %s %s has been completed. " +
                        "Transaction Reference: %s",
                event.getTransactionType(),
                event.getAmount(),
                event.getCurrency(),
                event.getTransactionRef()
        );
    }

    private String buildCreditMessage(TransactionEvent event) {
        return String.format(
                "You have received %s %s. " +
                        "Transaction Reference: %s",
                event.getAmount(),
                event.getCurrency(),
                event.getTransactionRef()
        );
    }

    private Map<String, Object> buildTemplateData(TransactionEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("transactionRef", event.getTransactionRef());
        data.put("amount", event.getAmount().toString());
        data.put("currency", event.getCurrency().toString());
        data.put("type", event.getTransactionType().toString());
        return data;
    }
}