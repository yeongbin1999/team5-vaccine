package com.back.domain.order.dto.order;

import com.back.domain.order.dto.orderitem.OrderItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Integer userId,
        
        @NotNull(message = "배송 정보 ID는 필수입니다.")
        Integer deliveryId,
        
        @NotBlank(message = "배송 주소는 필수입니다.")
        @Size(max = 200, message = "배송 주소는 200자를 초과할 수 없습니다.")
        String address,
        
        @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequestDTO> items
) {}
