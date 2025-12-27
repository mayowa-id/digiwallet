package com.fintech.digiwallet.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@digitalwallet.com}")
    private String fromEmail;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML email

            mailSender.send(message);

            log.info("📧 Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {}", to, e);
        }
    }

    public String buildTransactionEmail(String recipientName, String transactionType,
                                        String amount, String currency, String transactionRef) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .transaction-box { background: white; padding: 20px; border-radius: 8px; 
                                      margin: 20px 0; border-left: 4px solid #667eea; }
                    .amount { font-size: 32px; font-weight: bold; color: #667eea; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 30px; background: #667eea; 
                             color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💰 Transaction Notification</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>Your <strong>%s</strong> transaction has been processed successfully!</p>
                        
                        <div class="transaction-box">
                            <p style="margin: 0; color: #666;">Amount</p>
                            <p class="amount">%s %s</p>
                            <hr style="border: none; border-top: 1px solid #eee; margin: 15px 0;">
                            <p style="margin: 5px 0;"><strong>Transaction Reference:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Type:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Status:</strong> <span style="color: #10b981;">✓ Completed</span></p>
                        </div>
                        
                        <p>Need help? Contact our support team anytime.</p>
                        
                        <a href="#" class="button">View Transaction</a>
                    </div>
                    <div class="footer">
                        <p>© 2025 Digital Wallet. All rights reserved.</p>
                        <p>This is an automated message, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(recipientName, transactionType, amount, currency, transactionRef, transactionType);
    }

    public String buildScheduledPaymentEmail(String recipientName, String amount,
                                             String currency, String transactionRef) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .alert-box { background: #fff3cd; border-left: 4px solid #ffc107; 
                                padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .amount { font-size: 32px; font-weight: bold; color: #f5576c; }
                    .success-icon { font-size: 48px; text-align: center; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏰ Scheduled Payment Processed</h1>
                    </div>
                    <div class="content">
                        <div class="success-icon">✅</div>
                        <p>Hi %s,</p>
                        <p>Your scheduled payment has been processed automatically!</p>
                        
                        <div class="alert-box">
                            <p style="margin: 0; color: #666;">Payment Amount</p>
                            <p class="amount">%s %s</p>
                            <hr style="border: none; border-top: 1px solid #eee; margin: 15px 0;">
                            <p style="margin: 5px 0;"><strong>Reference:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Status:</strong> <span style="color: #10b981;">✓ Completed</span></p>
                        </div>
                        
                        <p>This was an automated payment. If you didn't expect this, please contact support immediately.</p>
                    </div>
                    <div style="text-align: center; padding: 20px; color: #666; font-size: 12px;">
                        <p>© 2025 Digital Wallet. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(recipientName, amount, currency, transactionRef);
    }
}