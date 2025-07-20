package com.back.domain.admin.controller;

import com.back.domain.order.dto.order.OrderStatusUpdateDTO;
import com.back.domain.order.entity.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminOrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderStatusUpdateDTO validStatusUpdateDTO;
    private OrderStatusUpdateDTO invalidStatusUpdateDTO;

    @BeforeEach
    void setUp() {
        // 유효한 주문 상태 변경 DTO
        validStatusUpdateDTO = new OrderStatusUpdateDTO(1, OrderStatus.배송중);

        // 유효성 검증 실패 테스트용 DTO
        invalidStatusUpdateDTO = new OrderStatusUpdateDTO(null, null);
    }

    // ========== 관리자 전체 주문 조회 API 테스트 ==========

    @Test
    @DisplayName("GET /api/v1/admin/orders - 전체 주문 조회 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders - 전체 주문 조회 성공 (상태 필터링)")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_Success_WithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "배송준비중"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders - 전체 주문 조회 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void getAllOrders_Forbidden_WithUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders - 전체 주문 조회 실패 (인증 없음)")
    void getAllOrders_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== 관리자 주문 상세 조회 API 테스트 ==========

    @Test
    @DisplayName("GET /api/v1/admin/orders/{orderId} - 주문 상세 조회 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getOrderDetail_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/{orderId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/{orderId} - 주문 상세 조회 실패 (주문 없음)")
    @WithMockUser(roles = "ADMIN")
    void getOrderDetail_NotFound_NonExistentOrder() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/{orderId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/{orderId} - 주문 상세 조회 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void getOrderDetail_Forbidden_WithUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/{orderId}", 1))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ========== 주문 상태 변경 API 테스트 ==========

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 주문 상태 변경 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_Success_WithAdmin() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStatusUpdateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("배송중"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 주문 상태 변경 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void updateOrderStatus_Forbidden_WithUser() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStatusUpdateDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 주문 상태 변경 실패 (인증 없음)")
    void updateOrderStatus_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStatusUpdateDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 주문 상태 변경 실패 (주문 없음)")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_NotFound_NonExistentOrder() throws Exception {
        OrderStatusUpdateDTO updateDto = new OrderStatusUpdateDTO(9999, OrderStatus.배송완료);
        
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 주문 상태 변경 실패 (유효성 검증 실패)")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_BadRequest_ValidationFailed() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStatusUpdateDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== 주문 통계 API 테스트 ==========

    @Test
    @DisplayName("GET /api/v1/admin/orders/statistics - 주문 통계 조회 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getOrderStatistics_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/statistics")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/statistics - 주문 통계 조회 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void getOrderStatistics_Forbidden_WithUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/statistics")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/statistics - 주문 통계 조회 실패 (인증 없음)")
    void getOrderStatistics_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/statistics")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/statistics - 주문 통계 조회 실패 (날짜 파라미터 누락)")
    @WithMockUser(roles = "ADMIN")
    void getOrderStatistics_BadRequest_MissingDateParams() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/statistics"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== 다양한 상태 변경 테스트 ==========

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 주문 취소로 상태 변경 성공")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_Success_CancelOrder() throws Exception {
        OrderStatusUpdateDTO cancelDto = new OrderStatusUpdateDTO(1, OrderStatus.취소);
        
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("취소"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 배송완료로 상태 변경 성공")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_Success_CompleteDelivery() throws Exception {
        OrderStatusUpdateDTO completeDto = new OrderStatusUpdateDTO(2, OrderStatus.배송완료);
        
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("배송완료"));
    }

    // ========== 페이지네이션 관련 테스트 ==========

    @Test
    @DisplayName("GET /api/v1/admin/orders - 페이지네이션 파라미터 검증")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_Success_PaginationParams() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .param("page", "1")
                        .param("size", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders - 잘못된 상태 필터 (존재하지 않는 상태)")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_BadRequest_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .param("status", "잘못된상태"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== 엣지 케이스 테스트 ==========

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 동일한 상태로 변경 시도")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_Success_SameStatus() throws Exception {
        // 주문 1번은 이미 '배송준비중' 상태
        OrderStatusUpdateDTO sameStatusDto = new OrderStatusUpdateDTO(1, OrderStatus.배송준비중);
        
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sameStatusDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("배송준비중"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/statistics - 유효한 날짜 범위 (같은 날)")
    @WithMockUser(roles = "ADMIN")
    void getOrderStatistics_Success_SameDate() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/statistics")
                        .param("startDate", "2024-06-15")
                        .param("endDate", "2024-06-15"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ========== 추가 테스트 (Product 스타일에 맞춘) ==========

    @Test
    @DisplayName("GET /api/v1/admin/orders - 기본 페이지네이션 (파라미터 없음)")
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_Success_DefaultPagination() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - Content-Type 누락")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_BadRequest_MissingContentType() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .content(objectMapper.writeValueAsString(validStatusUpdateDTO)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders/statistics - 잘못된 날짜 형식")
    @WithMockUser(roles = "ADMIN")
    void getOrderStatistics_BadRequest_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders/statistics")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2024-12-31"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderId}/status - 잘못된 JSON 형식")
    @WithMockUser(roles = "ADMIN")
    void updateOrderStatus_BadRequest_InvalidJson() throws Exception {
        String invalidJson = "{\"invalidField\": \"invalidValue\"}";
        
        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
