package com.example.commerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paystack")
@Getter
@Setter
public class PaystackProperties {
    private String secretKey;
    private String baseUrl = "https://api.paystack.co";
}
