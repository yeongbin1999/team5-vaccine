package com.back.global.exception;

import com.back.domain.product.exception.CategoryHasChildrenException;
import com.back.domain.product.exception.CategoryHasProductsException;
import com.back.domain.product.exception.CategoryNotFoundException;
import com.back.domain.product.exception.ProductNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryHasProductsException.class)
    public ResponseEntity<Map<String, String>> handleCategoryHasProductsException(
            CategoryHasProductsException e
    ) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CategoryHasChildrenException.class)
    public ResponseEntity<Map<String, String>> handleCategoryHasChildrenException(
            CategoryHasChildrenException e
    ) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotFoundException(
            CategoryNotFoundException e
    ) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(
            ProductNotFoundException e
    ) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String message = "데이터 무결성 제약조건 위반으로 인해 요청을 처리할 수 없습니다.";

        // 더 구체적인 에러 메시지 제공
        if (e.getMessage() != null && e.getMessage().contains("FOREIGN KEY")) {
            if (e.getMessage().contains("CART_ITEM")) {
                message = "이 상품은 장바구니에서 사용 중이므로 삭제할 수 없습니다.";
            } else if (e.getMessage().contains("ORDER_ITEM")) {
                message = "이 상품은 주문에서 사용 중이므로 삭제할 수 없습니다.";
            } else {
                message = "이 상품은 다른 데이터에서 참조되고 있어 삭제할 수 없습니다.";
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
