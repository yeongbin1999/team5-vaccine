package com.back.domain.order.repository;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 사용자 기준으로 주문 조회 (최신순)
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    // 기존 메서드 호환성 유지
    List<Order> findByUser(User user);

    // 주문 상태별 조회
    List<Order> findByStatus(OrderStatus status);
    
    // 주문 상태별 조회 (최신순)
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);
    
    // 주문 상태별 조회 (페이지네이션, 최신순)
    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);
    
    // 전체 주문 조회 (최신순) - 관리자용
    List<Order> findAllByOrderByOrderDateDesc();
    
    // 사용자와 주문 상태로 조회
    List<Order> findByUserAndStatus(User user, OrderStatus status);

    // 특정 기간 내의 주문을 조회 (일별/월별 판매액 통계용)
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    // 특정 기간과 상태로 주문 조회
    List<Order> findByOrderDateBetweenAndStatus(LocalDateTime start, LocalDateTime end, OrderStatus status);

    // 페이지네이션을 지원하는 전체 주문 조회 (관리자용)
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    
    // 주문 ID로 사용자 권한 확인을 위한 조회
    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findByIdAndUserId(@Param("orderId") Integer orderId, @Param("userId") Integer userId);
    
    // 사용자별 주문 개수
    long countByUser(User user);
    
    // 특정 상태의 주문 개수
    long countByStatus(OrderStatus status);
}
