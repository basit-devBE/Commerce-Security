package com.example.commerce.dtos.responses;

import com.example.commerce.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String stripeSessionId;
    private String paymentUrl;      // Redirect the user here to complete payment
    private Long amount;            // In cents (e.g. 4999 = $49.99)
    private String currency;
    private PaymentStatus status;
}
