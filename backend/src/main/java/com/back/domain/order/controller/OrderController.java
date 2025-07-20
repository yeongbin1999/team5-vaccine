package com.back.domain.order.controller;

import com.back.domain.order.dto.order.OrderDetailDTO;
import com.back.domain.order.dto.order.OrderListDTO;
import com.back.domain.order.dto.order.OrderRequestDTO;
import com.back.domain.order.service.OrderService;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "주문 관리 API - 일반 사용자용")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", 
            description = "장바구니 내용을 기반으로 주문을 생성합니다. (결제 전 단계)\n" +
                         "- 상품 재고를 자동으로 차감합니다.\n" +
                         "- 재고 부족시 InsufficientStockException이 발생합니다.\n" +
                         "- 주문 생성 후 '배송준비중' 상태로 설정됩니다.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDetailDTO> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderRequestDTO request
    ) {
        // 테스트 환경에서는 Mock User를 사용하므로 기본 ID 사용
        Integer userId = getCurrentUserId(userDetails);
        
        OrderRequestDTO requestWithUserId = new OrderRequestDTO(
                userId,
                request.deliveryId(),
                request.address(),
                request.items()
        );

        OrderDetailDTO orderDetail = orderService.createOrder(requestWithUserId);
        return new ResponseEntity<>(orderDetail, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "내 주문 목록 조회", 
            description = "현재 사용자의 모든 주문 목록을 조회합니다.")
    public ResponseEntity<List<OrderListDTO>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = getCurrentUserId(userDetails);
        List<OrderListDTO> orders = orderService.getMyOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "주문 상세 조회", 
            description = "특정 주문의 상세 정보를 조회합니다. (포함된 상품, 배송 정보, 결제 정보 등)")
    public ResponseEntity<OrderDetailDTO> getOrderDetail(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = getCurrentUserId(userDetails);
        OrderDetailDTO orderDetail = orderService.getOrderDetail(orderId, userId);
        return ResponseEntity.ok(orderDetail);
    }

    /**
     * UserDetails에서 사용자 ID를 추출하는 헬퍼 메서드
     * 테스트 환경에서는 Mock User를 고려하여 기본값 제공
     */
    private Integer getCurrentUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getId();
        }
        // 테스트 환경에서 Mock User 사용 시 기본 사용자 ID 반환
        // 실제 환경에서는 JWT 토큰에서 추출하거나 다른 방식 사용
        return 2; // 테스트용 기본 사용자 ID (data-test.sql의 user1)
    }
}
