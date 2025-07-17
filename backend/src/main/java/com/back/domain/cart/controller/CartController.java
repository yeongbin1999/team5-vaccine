package com.back.domain.cart.controller;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.CartDto;
import com.back.domain.cart.dto.UpdateCartItemRequest;
import com.back.domain.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    // CartController는 CartService를 사용하여 장바구니 관련 요청을 처리합니다.
    private final CartService cartService;

    // 현재는 로그인 유저 인증 기반 시스템이 아니기에 userId 파라미터를 받도록 처리했습니다.
    @GetMapping
    @Operation(summary = "장바구니 조회")
    public ResponseEntity<CartDto> getCart(@RequestParam Integer userId) {
        CartDto cartDto = cartService.getCart(userId);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/items")
    @Operation(summary = "장바구니에 상품 추가")
    public ResponseEntity<CartDto> addItem(@RequestParam Integer userId,
                                           @RequestBody AddCartItemRequest request) {
        cartService.addItem(userId, request);
        return ResponseEntity.ok().build();

    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 항목 수량 수정")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable Integer cartItemId,
                                                   @RequestBody UpdateCartItemRequest request) {
        cartService.updateItemQuantity(cartItemId, request.quantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 항목 삭제")
    public ResponseEntity<Void> deleteItem(@PathVariable Integer cartItemId) {
        cartService.deleteItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "장바구니 비우기")
    public ResponseEntity<Void> clearCart(@RequestParam Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

}
