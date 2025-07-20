package com.back.domain.order.dto.order;

import com.back.domain.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

//결제 처리 후 주문 상태변경
public record OrderStatusUpdateDTO(
        @NotNull(message = "주문 ID는 필수입니다.")
        Integer orderId,
        
        @NotNull(message = "변경할 상태는 필수입니다.")
        OrderStatus newStatus
) {}
