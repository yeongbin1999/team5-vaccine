package com.back.domain.order.dto.orderitem;

import com.back.domain.order.entity.OrderItem;

//주문 상세 조회에서 하나하나의 상품 응답용
public record OrderItemDetailDTO(
        Integer productId,
        String productName,
        Integer quantity,
        Integer unitPrice,
        Integer totalPrice
){
    public static OrderItemDetailDTO from(OrderItem item) {
        return new OrderItemDetailDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
