package com.back.domain.cart.controller;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.CartDto;
import com.back.domain.cart.dto.UpdateCartItemRequest;
import com.back.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // HttpStatus 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException; // NoSuchElementException 임포트

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "장바구니 조회")
    public ResponseEntity<CartDto> getCart(@RequestParam @Positive(message = "사용자 ID는 양수여야 합니다.") Integer userId) {
        CartDto cartDto = cartService.getCart(userId);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/items")
    @Operation(summary = "장바구니에 상품 추가")
    public ResponseEntity<Void> addItem(@RequestParam @Positive(message = "사용자 ID는 양수여야 합니다.") Integer userId,
                                        @RequestBody @Valid AddCartItemRequest request) {
        cartService.addItem(userId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 항목 수량 수정")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable @Positive(message = "장바구니 항목 ID는 양수여야 합니다.") Integer cartItemId,
                                                   @RequestBody @Valid UpdateCartItemRequest request) {
        cartService.updateItemQuantity(cartItemId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 항목 삭제")
    public ResponseEntity<Void> deleteItem(@PathVariable @Positive(message = "장바구니 항목 ID는 양수여야 합니다.") Integer cartItemId) {
        cartService.deleteItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "장바구니 비우기")
    public ResponseEntity<Void> clearCart(@RequestParam @Positive(message = "사용자 ID는 양수여야 합니다.") Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    // NoSuchElementException 처리 핸들러 추가
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        errors.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    // IllegalArgumentException 처리 핸들러 추가 (재고 부족 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        errors.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}