package com.example.commerce.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@Slf4j
public class SecurityAuditAspect {

    private final Map<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @AfterReturning(pointcut = "execution(* com.example.commerce.services.UserService.loginUser(..))", returning = "result")
    public void logSuccessfulLogin(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        String email = args.length > 0 ? extractEmail(args[0]) : "unknown";
        
        failedLoginAttempts.remove(email);
        log.info("SECURITY AUDIT - LOGIN SUCCESS | User: {} | Time: {}", email, LocalDateTime.now());
    }

    @AfterThrowing(pointcut = "execution(* com.example.commerce.services.UserService.loginUser(..))", throwing = "error")
    public void logFailedLogin(JoinPoint joinPoint, Throwable error) {
        Object[] args = joinPoint.getArgs();
        String email = args.length > 0 ? extractEmail(args[0]) : "unknown";
        
        int attempts = failedLoginAttempts.merge(email, 1, Integer::sum);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            log.warn("SECURITY ALERT - BRUTE FORCE DETECTED | User: {} | Attempts: {} | Time: {}", 
                    email, attempts, LocalDateTime.now());
        } else {
            log.warn("SECURITY AUDIT - LOGIN FAILED | User: {} | Attempts: {} | Time: {}", 
                    email, attempts, LocalDateTime.now());
        }
    }

    @AfterReturning("execution(* com.example.commerce.services.UserService.addUser(..))")
    public void logUserRegistration(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String email = args.length > 0 ? extractEmail(args[0]) : "unknown";
        log.info("SECURITY AUDIT - USER REGISTERED | Email: {} | Time: {}", email, LocalDateTime.now());
    }

    private String extractEmail(Object arg) {
        try {
            return (String) arg.getClass().getMethod("getEmail").invoke(arg);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
