package com.back.domain.order.dto.order;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

//주문 목록 조회시 리스트 형태로 반환
public record OrderListDTO(
        Integer orderId,
        String username,
        LocalDateTime orderDate,
        Integer totalPrice,
        OrderStatus status
)  {
    public static OrderListDTO from(Order order) {
        return new OrderListDTO(
                order.getId(),
                order.getUser().getName(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getStatus()
        );
    }
}
