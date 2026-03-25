package com.example.commerce.dtos.responses;

import lombok.Data;

@Data
public class PaymentInitDTO {
    private boolean status;
    private String message;
    private PaystackResponseData data;
}
