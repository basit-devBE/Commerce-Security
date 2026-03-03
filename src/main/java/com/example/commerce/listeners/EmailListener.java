package com.example.commerce.listeners;

import com.example.commerce.events.UserRegisterationEvent;
import com.example.commerce.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailListener {

    private final EmailService emailService;

    public EmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleUserRegistered(UserRegisterationEvent event) {
        log.info("Received user registration event for email: {}", event.getEmail());
        emailService.sendWelcomeEmail(event.getEmail())
            .exceptionallyAsync(ex -> {
                log.error("Failed to send welcome email: {}", ex.getMessage());
                return null;
            });
    }
}
