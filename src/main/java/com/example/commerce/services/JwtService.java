package com.example.commerce.services;

import com.example.commerce.entities.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret.access}")
    private String accesssecretKey;
    @Value("${jwt.secret.refresh}")
    private String refreshsecretKey;

    public JwtService() {
    }

    public  String generateAccessToken(UserEntity user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .and()
                .signWith((SecretKey) getKey(accesssecretKey), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UserEntity user){
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .and()
                .signWith((SecretKey) getKey(refreshsecretKey), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateAccessToken(String token){
        try{
            Claims claims = extractAllClaims(token, accesssecretKey);
            return  claims.getExpiration().after(new Date());
        }catch (JwtException e){
            return false;
        }
    }

    public Boolean validateRefreshToken(String token){
        try{
            Claims claims = extractAllClaims(token, refreshsecretKey);
            return  claims.getExpiration().after(new Date());
        }catch (JwtException e){
            return false;
        }
    }

    public String extractEmailFromAccessToken(String token){
        return extractClaims(token, accesssecretKey, Claims::getSubject);
    }

    public String extractEmailFromRefreshToken(String token){
        return extractClaims(token, refreshsecretKey, Claims::getSubject);
    }

    private <T> T extractClaims(String token, String secretKey, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }





    public Claims extractAllClaims(String token, String secretKey) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserIdFromAccessToken(String token) {
        Claims claims = extractAllClaims(token, accesssecretKey);
        return claims.get("userId", Long.class);
    }



    private Key getKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String extractRoleFromAccessToken(String token) {
        Claims claims = extractAllClaims(token, accesssecretKey);
        return claims.get("role", String.class);
    }
}
