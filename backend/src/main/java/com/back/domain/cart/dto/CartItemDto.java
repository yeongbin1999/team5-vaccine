package com.back.domain.cart.dto;

import com.back.domain.cart.entity.CartItem;

public record CartItemDto(
        Integer id,
        int productId,
        String productName,
        int quantity,
        int unitPrice
) {
    public CartItemDto(CartItem item) {
        this(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getProduct().getPrice()
        );
    }
}
