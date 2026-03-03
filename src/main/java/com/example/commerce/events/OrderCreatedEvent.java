package com.example.commerce.events;

import lombok.Getter;

@Getter
public class OrderCreatedEvent {
    private final Long orderId;
    private final String customerEmail;
    private final String customerName;
    private final Double totalAmount;

    public OrderCreatedEvent(Long orderId, String customerEmail, String customerName, Double totalAmount) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
    }
}
