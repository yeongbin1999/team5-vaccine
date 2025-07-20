package com.back.domain.order.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException e) {
        log.warn("Order not found: {}", e.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
                "error", "ORDER_NOT_FOUND",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException e) {
        log.warn("Insufficient stock: {}", e.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
                "error", "INSUFFICIENT_STOCK",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedOrderAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedOrderAccess(UnauthorizedOrderAccessException e) {
        log.warn("Unauthorized order access: {}", e.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
                "error", "UNAUTHORIZED_ORDER_ACCESS",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.FORBIDDEN.value()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
