package com.back.domain.order.dto.orderitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
        @NotNull(message = "상품 ID는 필수입니다.")
        @Min(value = 1, message = "상품 ID는 1 이상이어야 합니다.")
        Integer productId,
        
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Integer quantity,
        
        @NotNull(message = "단가는 필수입니다.")
        @Min(value = 0, message = "단가는 0 이상이어야 합니다.")
        Integer unitPrice
) {}
