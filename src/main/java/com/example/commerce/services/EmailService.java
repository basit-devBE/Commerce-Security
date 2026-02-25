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
}
