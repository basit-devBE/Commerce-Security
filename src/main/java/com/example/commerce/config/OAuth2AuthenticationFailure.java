package com.example.commerce.config;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationFailure extends SimpleUrlAuthenticationFailureHandler {
}
