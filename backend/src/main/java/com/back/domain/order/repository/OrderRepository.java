package com.back.domain.order.repository;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 사용자 기준으로 주문 조회
    List<Order> findByUser(User user);

    // 특정 기간 내의 주문을 조회 (일별/월별 판매액 통계용)
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    // 모든 주문 항목과 관련 상품을 함께 조회 (상품별 판매량 통계용)
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.product")
    List<OrderItem> findAllOrderItemsWithProduct();
}
