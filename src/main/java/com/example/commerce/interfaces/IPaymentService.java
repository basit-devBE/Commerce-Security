package com.example.commerce.interfaces;

import com.example.commerce.dtos.responses.PaymentResponse;

public interface IPaymentService {

    /**
     * Creates a Stripe Checkout Session for the given order.
     * Returns a PaymentResponse containing the hosted paymentUrl
     * to redirect the user to for payment.
     */
    PaymentResponse createCheckoutSession(Long userId, Long orderId);

    /**
     * Retrieves the current payment session for a given order.
     */
    PaymentResponse getPaymentByOrderId(Long orderId);

    /**
     * Handles Stripe Webhook events to securely update the payment status.
     */
    void handleStripeWebhook(String payload, String sigHeader);
}
