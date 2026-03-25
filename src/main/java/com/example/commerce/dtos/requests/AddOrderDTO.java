package com.example.commerce.dtos.requests;

import com.example.commerce.enums.PaymentChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddOrderDTO {
    @NotNull(message = "Payment channel is required")
    private PaymentChannel paymentChannel;
}
