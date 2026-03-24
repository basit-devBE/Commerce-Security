package com.example.commerce.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    @Setter
    @Getter
    private String secretKey;
    @Setter
    @Getter
    private String webhookSecret;

}
