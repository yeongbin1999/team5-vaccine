package com.back.domain.order.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String productName, int requestedQuantity, int availableStock) {
        super(String.format("상품 '%s'의 재고가 부족합니다. 요청 수량: %d, 사용 가능한 재고: %d", 
                productName, requestedQuantity, availableStock));
    }
}
