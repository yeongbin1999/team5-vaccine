package com.back.domain.cart.dto;

import com.back.domain.cart.entity.Cart;

import java.util.List;

public record CartDto(
        Integer cartId, // Long -> Integer 변경 제안
        int totalQuantity,
        int totalPrice,
        List<CartItemDto> items
) {
    public static CartDto from(Cart cart) {
        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(CartItemDto::new)
                .toList();

        int totalQuantity = itemDtos.stream().mapToInt(CartItemDto::quantity).sum();
        // CartItemDto에 unitPrice()가 있다고 가정합니다.
        int totalPrice = itemDtos.stream().mapToInt(item -> item.quantity() * item.unitPrice()).sum();

        return new CartDto(cart.getId(), totalQuantity, totalPrice, itemDtos);
    }
}
