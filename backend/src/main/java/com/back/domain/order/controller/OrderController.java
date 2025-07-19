package com.back.domain.order.controller;

import com.back.domain.order.dto.order.*;
import com.back.domain.order.dto.orderpay.OrderPayDTO;
import com.back.domain.order.service.OrderService;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderDetailDTO createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody OrderRequestDTO request) {
        request.setUserId(userDetails.getId());  // 주문자 ID 설정
        return orderService.createOrder(request);
    }

    // 2. 주문 결제
    @PostMapping("/{orderId}/pay")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderDetailDTO payForOrder(@PathVariable int orderId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody OrderPayDTO dto) {
        return orderService.payForOrder(orderId, dto, userDetails.getId()); // 사용자 ID 전달
    }

    // 3. 내 주문 목록 조회
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<OrderListDTO> getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return orderService.getMyOrders(userDetails.getId());
    }

    // 4. 주문 상세 조회 (유저 본인의 주문만 가능)
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderDetailDTO getOrderDetail(@PathVariable int orderId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        return orderService.getOrderDetail(orderId, userDetails.getId());
    }

    // 5. 관리자 - 전체 주문 목록 조회
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderListDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    // 6. 관리자 - 주문 상태 변경
    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDetailDTO updateOrderStatus(@PathVariable int orderId,
                                            @RequestBody OrderStatusUpdateDTO dto) {
        return orderService.updateOrderStatus(new OrderStatusUpdateDTO(orderId, dto.newStatus()));
    }
}
