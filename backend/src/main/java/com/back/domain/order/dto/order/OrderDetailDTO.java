package com.back.domain.order.dto.order;

import com.back.domain.order.dto.orderitem.OrderItemDetailDTO;
import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

//주문 상세 페이지에서 하나의 주문을 자세히 보여줄 때 응답용
public record OrderDetailDTO(
        Integer orderId,
        String address,
        Integer totalPrice,
        OrderStatus status,
        LocalDateTime orderDate,
        String username,
        List<OrderItemDetailDTO> items
){
    public static OrderDetailDTO from(Order order, List<OrderItem> orderItems) {
        List<OrderItemDetailDTO> itemDTOs = orderItems.stream()
                .map(OrderItemDetailDTO::from)
                .toList();

        return new OrderDetailDTO(
                order.getId(),
                order.getAddress(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getOrderDate(),
                order.getUser().getName(),
                itemDTOs
        );
    }
}

