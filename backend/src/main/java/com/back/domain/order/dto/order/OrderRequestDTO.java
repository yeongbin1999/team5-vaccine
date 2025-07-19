package com.back.domain.order.dto.order;

import com.back.domain.order.dto.orderitem.OrderItemRequestDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDTO {
    private int userId;
    private int deliveryId;
    private String shippingAddress;
    private List<OrderItemRequestDTO> items;
}
