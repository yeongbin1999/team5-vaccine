package com.back.domain.admin.controller;

import com.back.domain.admin.dto.OrderStatisticsResponseDto;
import com.back.domain.admin.dto.PageResponseDto;
import com.back.domain.order.dto.order.OrderDetailDTO;
import com.back.domain.order.dto.order.OrderListDTO;
import com.back.domain.order.dto.order.OrderStatusUpdateDTO;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Order", description = "관리자 주문 관리 API")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "관리자 - 모든 주문 조회",
            description = "모든 사용자의 주문 목록을 조회합니다. 페이지네이션, 검색, 필터링이 가능합니다.")
    public ResponseEntity<PageResponseDto<OrderListDTO>> getAllOrders(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "주문 상태 필터") @RequestParam(required = false) OrderStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderListDTO> orders = orderService.getAllOrdersWithPagination(pageable, status);
        
        PageResponseDto<OrderListDTO> response = PageResponseDto.of(orders);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "관리자 - 주문 통계 조회",
            description = "지정된 기간의 주문 통계를 조회합니다.")
    public ResponseEntity<List<OrderStatisticsResponseDto>> getOrderStatistics(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<OrderStatisticsResponseDto> statistics = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "관리자 - 주문 상태 변경",
            description = "특정 주문의 상태를 변경합니다. (예: 배송준비중, 배송중, 배송완료, 취소)")
    public ResponseEntity<OrderDetailDTO> updateOrderStatus(
            @PathVariable Integer orderId,
            @Valid @RequestBody OrderStatusUpdateDTO dto) {
        OrderDetailDTO updatedOrder = orderService.updateOrderStatus(
                new OrderStatusUpdateDTO(orderId, dto.newStatus()));
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "관리자 - 특정 주문 상세 조회",
            description = "관리자가 특정 주문의 상세 정보를 조회합니다.")
    public ResponseEntity<OrderDetailDTO> getOrderDetail(@PathVariable Integer orderId) {
        OrderDetailDTO orderDetail = orderService.getOrderDetailForAdmin(orderId);
        return ResponseEntity.ok(orderDetail);
    }
}
