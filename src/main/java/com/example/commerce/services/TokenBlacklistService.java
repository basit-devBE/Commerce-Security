package com.example.commerce.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        Long expirationTime = jwtService.extractAllAccessClaims(token)
                .getExpiration().getTime();
        blacklistedTokens.put(token, expirationTime);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }


    @Scheduled(fixedRate = 6000000) // Run every 100 minutes
    public void cleanupExpiredTokens() {
        log.info("Running token blacklist cleanup...");
        long currentTime = new Date().getTime();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
}
