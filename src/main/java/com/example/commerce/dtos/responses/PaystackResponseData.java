package com.example.commerce.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaystackResponseData {
    @JsonProperty("authorization_url")
    private String authorizationUrl;
    
    @JsonProperty("access_code")
    private String accessCode;
    
    private String reference;
}
