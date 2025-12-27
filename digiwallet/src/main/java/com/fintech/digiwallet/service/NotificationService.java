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
    private final EmailService emailService;

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
            String emailBody = emailService.buildTransactionEmail(
                    event.getSourceUserEmail().split("@")[0],
                    event.getTransactionType().toString(),
                    event.getAmount().toString(),
                    event.getCurrency().toString(),
                    event.getTransactionRef()
            );

            emailService.sendEmail(
                    event.getSourceUserEmail(),
                    "Transaction Completed - " + event.getTransactionRef(),
                    emailBody
            );
        }

        // Send notification to destination user (if exists)
        if (event.getDestinationUserEmail() != null) {
            String emailBody = emailService.buildTransactionEmail(
                    event.getDestinationUserEmail().split("@")[0],
                    "CREDIT",
                    event.getAmount().toString(),
                    event.getCurrency().toString(),
                    event.getTransactionRef()
            );

            emailService.sendEmail(
                    event.getDestinationUserEmail(),
                    "Funds Received - " + event.getTransactionRef(),
                    emailBody
            );
        }
    }

    public void sendScheduledPaymentNotification(String userEmail,
                                                 String transactionRef,
                                                 String amount) {
        log.info("SENDING SCHEDULED PAYMENT EMAIL to: {}", userEmail);

        String[] parts = amount.split(" ");
        String amountValue = parts[0];
        String currency = parts.length > 1 ? parts[1] : "USD";

        String emailBody = emailService.buildScheduledPaymentEmail(
                userEmail.split("@")[0],
                amountValue,
                currency,
                transactionRef
        );

        emailService.sendEmail(
                userEmail,
                "Scheduled Payment Processed - " + transactionRef,
                emailBody
        );

        // Also log to console for demo
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCHEDULED PAYMENT EMAIL SENT!");
        System.out.println("=".repeat(80));
        System.out.println("To: " + userEmail);
        System.out.println("Amount: " + amount);
        System.out.println("Reference: " + transactionRef);
        System.out.println("=".repeat(80) + "\n");
    }

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-sender-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processNotification(NotificationEvent event) {
        log.info("📬 Processing notification for: {}", event.getRecipient());

        // Log to console for demo visibility
        System.out.println("\n" + "=".repeat(80));
        System.out.println("NEW NOTIFICATION");
        System.out.println("=".repeat(80));
        System.out.println("To: " + event.getRecipient());
        System.out.println("Subject: " + event.getSubject());
        System.out.println("Message: " + event.getMessage());
        System.out.println("Time: " + event.getTimestamp());
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
}}