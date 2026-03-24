package com.example.commerce.repositories;

import com.example.commerce.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    // Look up the payment session for a given order
    Optional<PaymentEntity> findByOrderId(Long orderId);

    // Used by the webhook to update status after Stripe confirms payment
    Optional<PaymentEntity> findByStripeSessionId(String stripeSessionId);
}
