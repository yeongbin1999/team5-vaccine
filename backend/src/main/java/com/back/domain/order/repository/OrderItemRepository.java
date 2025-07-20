package com.back.domain.order.repository;

import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    List<OrderItem> findByOrder(Order order);
    
    // 모든 주문 항목과 관련 상품을 함께 조회 (상품별 판매량 통계용)
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product")
    List<OrderItem> findAllWithProduct();
    
    // 특정 상품의 모든 주문 항목 조회
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(Integer productId);
}
