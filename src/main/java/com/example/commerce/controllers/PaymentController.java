package com.example.commerce.controllers;

import com.example.commerce.dtos.responses.ApiResponse;
import com.example.commerce.dtos.responses.PaymentResponse;
import com.example.commerce.interfaces.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Creates a Stripe checkout session for the given order.
     * Returns a paymentUrl to redirect the user to for payment.
     */
    @Operation(summary = "Create checkout session for an order",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> createCheckoutSession(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("authenticatedUserId");
        PaymentResponse response = paymentService.createCheckoutSession(userId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Checkout session created", response));
    }

    /**
     * Get the payment session for a given order.
     */
    @Operation(summary = "Get payment session by order ID",
               security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Payment session fetched", response));
    }

    /**
     * Stripe webhook receiver — Stripe posts here when payment events happen.
     * TODO: Implement signature verification and status update logic.
     * Security is intentionally open — verified via Stripe-Signature header instead of JWT.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            paymentService.handleStripeWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Paystack webhook receiver — Paystack posts here when MOMO payment events happen.
     */
    @PostMapping("/paystack/webhook")
    public ResponseEntity<Void> handlePaystackWebhook(
            @RequestBody com.example.commerce.dtos.responses.PaystackWebhookPayload payload,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {
        try {
            paymentService.handlePaystackWebhook(payload, signature);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
