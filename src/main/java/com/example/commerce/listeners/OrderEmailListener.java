package com.example.commerce.listeners;

import com.example.commerce.events.OrderCreatedEvent;
import com.example.commerce.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEmailListener {

    private final EmailService emailService;

    public OrderEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("taskExecutor")
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event for order ID: {}", event.getOrderId());
        emailService.sendOrderConfirmation(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getOrderId(),
                event.getTotalAmount()
        ).exceptionallyAsync(ex -> {
            log.error("Failed to send order confirmation email: {}", ex.getMessage());
            return null;
        });
    }
}
