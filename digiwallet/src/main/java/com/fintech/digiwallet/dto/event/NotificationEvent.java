package com.fintech.digiwallet.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationEvent {
    private String recipient; // email or phone
    private String notificationType; // EMAIL, SMS, PUSH
    private String subject;
    private String message;
    private Map<String, Object> templateData;
    private LocalDateTime timestamp;
}