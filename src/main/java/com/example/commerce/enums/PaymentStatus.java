package com.example.commerce.enums;

public enum PaymentStatus {
    PENDING,    // Checkout session created, user hasn't paid yet
    COMPLETED,  // Stripe confirmed payment was successful
    FAILED,     // Payment attempt failed
    EXPIRED     // Session expired before payment was completed
}
