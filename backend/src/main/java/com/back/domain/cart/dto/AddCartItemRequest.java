package com.back.domain.cart.dto;

public record AddCartItemRequest(
        Integer productId,
        int quantity
) {}