package com.back.domain.order.dto.order;

import com.back.domain.order.dto.orderitem.OrderItemRequestDTO;

import java.util.List;

public record OrderRequestDTO(
        int userId,
        int deliveryId,
        String address,
        List<OrderItemRequestDTO> items
) {}

