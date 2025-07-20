package com.back.domain.order.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedOrderAccessException() {
        super("본인의 주문만 조회할 수 있습니다.");
    }
}
