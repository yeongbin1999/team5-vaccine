package com.back.domain.order.dto.orderitem;

//주문 생성시 상품 정보 담기
public record OrderItemRequestDTO(
        int productId,
        int quantity,
        int unitPrice
){}
