package com.back.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 0, message = "수량은 0개 이상이어야 합니다.")
        int quantity
) {}
