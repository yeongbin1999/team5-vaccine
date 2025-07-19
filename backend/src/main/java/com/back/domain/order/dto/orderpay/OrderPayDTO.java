package com.back.domain.order.dto.orderpay;

public record OrderPayDTO(
        String paymentMethod,
        String paymentDetails
) {}
