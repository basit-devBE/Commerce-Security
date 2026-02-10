package com.example.commerce.config;

import com.example.commerce.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtService jwtService;
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        logger.info("Received authHeader: " + authHeader);
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        if(!jwtService.validateAccessToken(token)){
            filterChain.doFilter(request, response);
            return;
        }
        Long userId = jwtService.extractUserIdFromAccessToken(token);
        String email = jwtService.extractEmailFromAccessToken(token);
        String role = jwtService.extractRoleFromAccessToken(token);
        var authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute("authenticatedUserId", userId);
        filterChain.doFilter(request, response);
    }
}