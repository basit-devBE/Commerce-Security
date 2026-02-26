package com.example.commerce.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> sendWelcomeEmail(String to) {
        logger.info("Starting to send welcome email to: {}", to);
        try {
            String subject = "Welcome to Commerce Security!";
            String body = buildWelcomeEmailBody();
            sendEmail(to, subject, body);
            logger.info("Welcome email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> sendOrderConfirmation(String to, String customerName, Long orderId, Double totalAmount) {
        logger.info("Starting to send order confirmation email to: {}", to);
        try {
            String subject = "Order Confirmation - Order #" + orderId;
            String body = buildOrderConfirmationBody(customerName, orderId, totalAmount);
            sendEmail(to, subject, body);
            logger.info("Order confirmation email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Failed to send order confirmation to {}: {}", to, e.getMessage(), e);
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setText(body, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("noreply@commerce-security.com");
        mailSender.send(message);
    }

    private String buildWelcomeEmailBody() {
        return """
            <html>
                <body>
                    <h2>Welcome to Commerce Security!</h2>
                    <p>Thank you for registering with us.</p>
                    <p>Your account has been successfully created.</p>
                    <p>Start shopping now!</p>
                </body>
            </html>
            """;
    }

    private String buildOrderConfirmationBody(String customerName, Long orderId, Double totalAmount) {
        return """
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Order Confirmation</h2>
                    <p>Dear %s,</p>
                    <p>Thank you for your order! Your order has been successfully placed.</p>
                    <div style="background-color: #f5f5f5; padding: 15px; margin: 20px 0;">
                        <p><strong>Order ID:</strong> #%d</p>
                        <p><strong>Total Amount:</strong> $%.2f</p>
                    </div>
                    <p>We'll send you another email when your order ships.</p>
                    <p>Thank you for shopping with us!</p>
                </body>
            </html>
            """.formatted(customerName, orderId, totalAmount);
    }
}
