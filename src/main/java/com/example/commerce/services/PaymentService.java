package com.example.commerce.services;

import com.example.commerce.config.StripeProperties;
import com.example.commerce.dtos.responses.PaymentResponse;
import com.example.commerce.entities.CartEntity;
import com.example.commerce.entities.OrderEntity;
import com.example.commerce.entities.PaymentEntity;
import com.example.commerce.entities.UserEntity;
import com.example.commerce.enums.OrderStatus;
import com.example.commerce.enums.PaymentStatus;
import com.example.commerce.errorhandlers.ResourceNotFoundException;
import com.example.commerce.events.OrderCreatedEvent;
import com.example.commerce.interfaces.IPaymentService;
import com.example.commerce.config.PaystackProperties;
import com.example.commerce.dtos.responses.PaymentInitDTO;
import com.example.commerce.dtos.responses.PaystackResponseData;
import com.example.commerce.dtos.responses.PaystackWebhookPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import java.util.HashMap;
import java.util.Map;
import com.example.commerce.repositories.CartRepository;
import com.example.commerce.repositories.OrderRepository;
import com.example.commerce.repositories.PaymentRepository;
import com.example.commerce.repositories.UserRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StripeProperties stripeProperties;
    private final CartRepository cartRepository;
    private final ApplicationEventPublisher publisher;
    private final PaystackProperties paystackProperties;
    private final RestTemplate restTemplate;

    public PaymentService(PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            StripeProperties stripeProperties,
            CartRepository cartRepository, ApplicationEventPublisher publisher,
            PaystackProperties paystackProperties,
            RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.stripeProperties = stripeProperties;
        this.cartRepository = cartRepository;
        this.publisher = publisher;
        this.paystackProperties = paystackProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public PaymentResponse createCheckoutSession(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the authenticated user");
        }

        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new IllegalArgumentException("A checkout session already exists for this order");
        });

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        long amountInCents = Math.round(order.getTotalAmount() * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/payment/cancel")
                .setCustomerEmail(user.getEmail())
                .putMetadata("orderId", orderId.toString())
                .putMetadata("userId", userId.toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Order #" + orderId)
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            Session session = Session.create(params);

            PaymentEntity payment = new PaymentEntity();
            payment.setOrder(order);
            payment.setAmount(amountInCents);
            payment.setCurrency("usd");
            payment.setStatus(PaymentStatus.PENDING);
            payment.setStripeSessionId(session.getId());
            payment.setPaymentUrl(session.getUrl());

            PaymentEntity saved = paymentRepository.save(payment);
            log.info("Created checkout session for order {} — payment ID {}", orderId, saved.getId());

            return toResponse(saved);

        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            throw new IllegalStateException("Failed to initialize payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse initializeMomoPayment(Long userId, Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the authenticated user");
        }

        paymentRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new IllegalArgumentException("A payment session already exists for this order");
        });

        long amountInCents = Math.round(order.getTotalAmount() * 100);

        try {
            String url = paystackProperties.getBaseUrl() + "/transaction/initialize";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(paystackProperties.getSecretKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amountInCents);
            requestBody.put("email", order.getUser().getEmail());
            requestBody.put("reference", orderId.toString() + "-" + System.currentTimeMillis());
            requestBody.put("callback_url", "http://localhost:3000/payment/success");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<PaymentInitDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PaymentInitDTO.class);

            log.info("Raw Paystack Response: {}", response.getBody());
            PaymentInitDTO paymentInitDTO = response.getBody();
            if (paymentInitDTO != null && paymentInitDTO.isStatus() && paymentInitDTO.getData() != null) {
                log.info("Paystack Auth URL: {}", paymentInitDTO.getData().getAuthorizationUrl());
                PaymentEntity payment = new PaymentEntity();
                payment.setOrder(order);
                payment.setAmount(amountInCents);
                payment.setCurrency("GHS"); // Assuming GHS for MOMO by default
                payment.setStatus(PaymentStatus.PENDING);
                // We reuse stripeSessionId to store the Paystack reference id for the webhook
                // verification
                payment.setStripeSessionId(paymentInitDTO.getData().getReference());
                payment.setPaymentUrl(paymentInitDTO.getData().getAuthorizationUrl());

                PaymentEntity saved = paymentRepository.save(payment);
                log.info("Initialized Paystack MOMO payment for order {} — payment ID {}", orderId, saved.getId());

                return toResponse(saved);
            } else {
                String message = paymentInitDTO != null ? paymentInitDTO.getMessage() : "No response from Paystack";
                throw new IllegalStateException("Failed to initialize payment with Paystack: " + message);
            }
        } catch (Exception e) {
            log.error("Error initializing Paystack payment for order {}: {}", orderId, e.getMessage(), e);
            throw new IllegalStateException("Payment initialization failed: " + e.getMessage());
        }

    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment session found for order ID: " + orderId));
        return toResponse(payment);
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            // Verify signature matches our webhook secret to ensure the request is truly
            // from Stripe
            Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = event.getDataObjectDeserializer().getObject()
                        .map(obj -> (Session) obj)
                        .orElseGet(() -> {
                            try {
                                return (Session) event.getDataObjectDeserializer().deserializeUnsafe();
                            } catch (EventDataObjectDeserializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
                log.info("Processing completed checkout session: {}", session.getId());
                PaymentEntity payment = paymentRepository.findByStripeSessionId(session.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Payment not found for session: " + session.getId()));
                log.info("Found payment entity for session {}: payment ID {}, current status {}", session.getId(),
                        payment.getId(), payment.getStatus());
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                OrderEntity order = payment.getOrder();
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                // Extract userId from Stripe payload metadata
                String userIdString = session.getMetadata().get("userId");
                if (userIdString != null) {
                    Long userId = Long.valueOf(userIdString);
                    CartEntity cart = cartRepository.findByUserId(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                    // clear cart items after successful payment
                    cart.getItems().clear();
                    cartRepository.save(cart);
                    OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order.getId(),
                            order.getUser().getEmail(), (user.getFirstName() + " " + user.getLastName()),
                            order.getTotalAmount());
                    publisher.publishEvent(orderCreatedEvent);
                    log.info("Order {} created for user {}", order.getId(), userId);
                }

                log.info("Payment session {} completed successfully", session.getId());

            } else if ("checkout.session.expired".equals(event.getType())) {
                Session session = event.getDataObjectDeserializer().getObject()
                        .map(obj -> (Session) obj)
                        .orElseGet(() -> {
                            try {
                                return (Session) event.getDataObjectDeserializer().deserializeUnsafe();
                            } catch (EventDataObjectDeserializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
                PaymentEntity payment = paymentRepository.findByStripeSessionId(session.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Payment not found for session: " + session.getId()));
                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);
                log.info("Payment session {} expired", session.getId());
            }
        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed", e);
            throw new IllegalArgumentException("Invalid Stripe signature");
        }
    }

    @Override
    @Transactional
    public void handlePaystackWebhook(PaystackWebhookPayload payload, String signature) {
        if (payload == null || payload.getEvent() == null || payload.getData() == null) {
            log.warn("Invalid Paystack webhook payload received");
            return;
        }

        if ("charge.success".equals(payload.getEvent())) {
            PaystackWebhookPayload.WebhookData data = payload.getData();
            String reference = data.getReference();

            PaymentEntity payment = paymentRepository.findByStripeSessionId(reference)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment not found for Paystack reference: " + reference));

            if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
                log.info("Payment {} already completed", payment.getId());
                return;
            }

            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            OrderEntity order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            Long userId = order.getUser().getId();
            CartEntity cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // clear cart items after successful payment
            cart.getItems().clear();
            cartRepository.save(cart);

            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order.getId(), order.getUser().getEmail(),
                    (user.getFirstName() + " " + user.getLastName()), order.getTotalAmount());
            publisher.publishEvent(orderCreatedEvent);
            log.info("Order {} marked as PAID via Paystack for user {}", order.getId(), userId);
        } else {
            log.info("Unhandled Paystack event type: {}", payload.getEvent());
        }
    }

    private PaymentResponse toResponse(PaymentEntity payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .stripeSessionId(payment.getStripeSessionId())
                .paymentUrl(payment.getPaymentUrl())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .build();
    }
}
