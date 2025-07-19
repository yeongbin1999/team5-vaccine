package com.back.domain.product.exception;

public class CategoryHasChildrenException extends RuntimeException {
    public CategoryHasChildrenException(String message) {
        super(message);
    }
    
    public CategoryHasChildrenException(String message, Throwable cause) {
        super(message, cause);
    }
}
