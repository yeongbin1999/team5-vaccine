package com.back.domain.cart.dto;

import com.back.domain.cart.entity.Cart;

import java.util.List;

public record CartDto(
        Long cartId,
        int totalQuantity,
        int totalPrice,
        List<CartItemDto> items
) {
    public static CartDto from(Cart cart) {
        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(CartItemDto::new)
                .toList();

        int totalQuantity = itemDtos.stream().mapToInt(CartItemDto::quantity).sum();
        int totalPrice = itemDtos.stream().mapToInt(item -> item.quantity() * item.unitPrice()).sum();

        return new CartDto(cart.getId(), totalQuantity, totalPrice, itemDtos);
    }
}