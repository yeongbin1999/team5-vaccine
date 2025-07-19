package com.back.domain.order.repository;

import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrder(Order order);
}
