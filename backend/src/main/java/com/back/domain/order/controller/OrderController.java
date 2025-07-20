package com.back.domain.order.controller;

import com.back.domain.order.dto.order.OrderDetailDTO;
import com.back.domain.order.dto.order.OrderListDTO;
import com.back.domain.order.dto.order.OrderRequestDTO;
import com.back.domain.order.dto.order.OrderStatusUpdateDTO;
import com.back.domain.order.service.OrderService;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    // 1. 주문 생성
    @PostMapping
    public ResponseEntity<OrderDetailDTO> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OrderRequestDTO request
    ) {
        // userId 포함된 새 DTO 생성
        OrderRequestDTO requestWithUserId = new OrderRequestDTO(
                userDetails.getId(),
                request.deliveryId(),
                request.address(),
                request.items()
        );

        OrderDetailDTO orderDetail = orderService.createOrder(requestWithUserId);
        return ResponseEntity.ok(orderDetail);
    }

    // 2. 내 주문 목록 조회
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<OrderListDTO> getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return orderService.getMyOrders(userDetails.getId());
    }

    // 3. 주문 상세 조회 (유저 본인의 주문만 가능)
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderDetailDTO getOrderDetail(@PathVariable int orderId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        return orderService.getOrderDetail(orderId, userDetails.getId());
    }

    // 4. 관리자 - 전체 주문 목록 조회
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderListDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    // 5. 관리자 - 주문 상태 변경
    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDetailDTO updateOrderStatus(@PathVariable int orderId,
                                            @RequestBody OrderStatusUpdateDTO dto) {
        return orderService.updateOrderStatus(new OrderStatusUpdateDTO(orderId, dto.newStatus()));
    }
}
