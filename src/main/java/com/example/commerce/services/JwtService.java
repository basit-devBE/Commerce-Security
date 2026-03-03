package com.example.commerce.services;

import com.example.commerce.entities.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret.access}")
    private String accessSecretKey;
    @Value("${jwt.secret.refresh}")
    private String refreshSecretKey;

    private SecretKey cachedAccessKey;
    private SecretKey cachedRefreshKey;

    @PostConstruct
    public void init() {
        this.cachedAccessKey = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
        this.cachedRefreshKey = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserEntity user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .and()
                .signWith(cachedAccessKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UserEntity user){
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .and()
                .signWith(cachedRefreshKey, Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateAccessToken(String token){
        try{
            Claims claims = extractAllAccessClaims(token);
            return claims.getExpiration().after(new Date());
        }catch (JwtException e){
            return false;
        }
    }

    public Boolean validateRefreshToken(String token){
        try{
            Claims claims = extractAllRefreshClaims(token);
            return claims.getExpiration().after(new Date());
        }catch (JwtException e){
            return false;
        }
    }

    public String extractEmailFromAccessToken(String token){
        return extractAllAccessClaims(token).getSubject();
    }

    public String extractEmailFromRefreshToken(String token){
        return extractAllRefreshClaims(token).getSubject();
    }

    public Claims extractAllAccessClaims(String token) {
        return Jwts.parser()
                .verifyWith(cachedAccessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims extractAllRefreshClaims(String token) {
        return Jwts.parser()
                .verifyWith(cachedRefreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserIdFromAccessToken(String token) {
        return extractAllAccessClaims(token).get("userId", Long.class);
    }

    public String extractRoleFromAccessToken(String token) {
        return extractAllAccessClaims(token).get("role", String.class);
    }
}