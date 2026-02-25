package com.example.commerce.services;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        Long expirationTime = jwtService.extractAllClaims(token, jwtService.getAccessSecretKey())
                .getExpiration().getTime();
        blacklistedTokens.put(token, expirationTime);
        cleanupExpiredTokens();
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    private void cleanupExpiredTokens() {
        long currentTime = new Date().getTime();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
}
