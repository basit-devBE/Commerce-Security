package com.example.commerce.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.example.commerce.controllers..*)")
    public void controllerLayer() {}
    
    @Pointcut("within(com.example.commerce.services..*)")
    public void serviceLayer() {}

    @Before("controllerLayer()")
    public void logBeforeControllerMethods(JoinPoint joinPoint) {
        log.info("Entering method: {}", joinPoint.getSignature().getName());
    }

    @After("controllerLayer()")
    public void logAfterControllerMethods(JoinPoint joinPoint) {
        log.info("Exiting method: {}", joinPoint.getSignature().getName());
    }
    
    @Before("serviceLayer() && @annotation(org.springframework.cache.annotation.Cacheable)")
    public void logCacheableMethods(JoinPoint joinPoint) {
        log.info("Fetching from cache or DB: {}", joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "controllerLayer() || serviceLayer()", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        log.error("Exception in method: {} with message: {}", joinPoint.getSignature().getName(), error.getMessage());
    }
}
