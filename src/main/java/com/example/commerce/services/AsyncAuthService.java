package com.example.commerce.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncAuthService {
    
    private final PasswordEncoder passwordEncoder;
    
    @Async("authExecutor")
    public CompletableFuture<Boolean> verifyPassword(String rawPassword, String encodedPassword) {
        return CompletableFuture.completedFuture(
            passwordEncoder.matches(rawPassword, encodedPassword)
        );
    }
}
