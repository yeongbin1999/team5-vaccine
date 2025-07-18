package com.back.domain.cart.dto;

import com.back.domain.cart.entity.Cart;

import java.util.List;
import java.util.stream.Collectors;

public record CartDto(
        Integer cartId,
        int totalQuantity,
        int totalPrice,
        List<CartItemDto> items
) {
    public static CartDto from(Cart cart) {
        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(CartItemDto::new)
                .collect(Collectors.toList());

        int calculatedTotalQuantity = itemDtos.stream()
                .mapToInt(CartItemDto::quantity)
                .sum();
        int calculatedTotalPrice = itemDtos.stream()
                .mapToInt(item -> item.quantity() * item.unitPrice())
                .sum();

        return new CartDto(
                cart.getId(),
                calculatedTotalQuantity,
                calculatedTotalPrice,
                itemDtos
        );
    }
}