package com.back.domain.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    
    public OrderNotFoundException(Integer orderId) {
        super("주문을 찾을 수 없습니다. ID: " + orderId);
    }
}
