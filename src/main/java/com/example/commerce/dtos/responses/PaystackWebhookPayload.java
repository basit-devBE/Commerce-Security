package com.example.commerce.dtos.responses;

import lombok.Data;

@Data
public class PaystackWebhookPayload {
    private String event;
    private WebhookData data;

    @Data
    public static class WebhookData {
        private String reference;
        private String status;
        private String channel;
        private Long amount;
        private String currency;
    }
}
