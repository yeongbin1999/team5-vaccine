package com.back.domain.order.controller;

import com.back.domain.order.dto.order.OrderRequestDTO;
import com.back.domain.order.dto.orderitem.OrderItemRequestDTO;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequestDTO testOrderRequestDTO;
    private OrderRequestDTO invalidOrderRequestDTO;

    @BeforeEach
    void setUp() {
        // 테스트용 주문 요청 DTO (배송 ID 1, 상품 1번과 2번 주문)
        List<OrderItemRequestDTO> testItems = List.of(
                new OrderItemRequestDTO(1, 2, 18000), // 에티오피아 예가체프 2개
                new OrderItemRequestDTO(2, 1, 17000)  // 콜롬비아 수프리모 1개
        );
        
        testOrderRequestDTO = new OrderRequestDTO(
                2, // 유저1 ID
                1, // 배송 ID
                "서울시 강남구 테헤란로 123",
                testItems
        );

        // 유효성 검증 실패 테스트용 DTO (빈 주소)
        invalidOrderRequestDTO = new OrderRequestDTO(
                2, // 유저1 ID
                1, // 배송 ID
                "", // 빈 주소
                List.of() // 빈 항목 리스트
        );
    }

    // ========== 주문 생성 API 테스트 (인증 필요) ==========

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 성공")
    @WithMockUser(roles = "USER")
    void createOrder_Success() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.address").value("서울시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.totalPrice").value(53000)) // 18000*2 + 17000*1
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items.length()").value(2)); // 2개 상품
    }

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 실패 (인증 없음)")
    void createOrder_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequestDTO)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 실패 (유효성 검증 실패 - 빈 주소)")
    @WithMockUser(roles = "USER")
    void createOrder_BadRequest_ValidationFailed() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderRequestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 실패 (재고 부족)")
    @WithMockUser(roles = "USER")
    void createOrder_BadRequest_InsufficientStock() throws Exception {
        // 재고 초과 주문 (상품 1번의 재고는 50개)
        List<OrderItemRequestDTO> excessiveItems = List.of(
                new OrderItemRequestDTO(1, 100, 18000) // 100개 주문 (재고 초과)
        );
        
        OrderRequestDTO excessiveOrderRequest = new OrderRequestDTO(
                2, 1, "서울시 강남구", excessiveItems
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excessiveOrderRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - 주문 생성 실패 (존재하지 않는 상품)")
    @WithMockUser(roles = "USER")
    void createOrder_NotFound_NonExistentProduct() throws Exception {
        List<OrderItemRequestDTO> invalidItems = List.of(
                new OrderItemRequestDTO(9999, 1, 18000) // 존재하지 않는 상품 ID
        );
        
        OrderRequestDTO invalidProductOrder = new OrderRequestDTO(
                2, 1, "서울시 강남구", invalidItems
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductOrder)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ========== 내 주문 목록 조회 API 테스트 (인증 필요) ==========

    @Test
    @DisplayName("GET /api/v1/orders - 내 주문 목록 조회 성공")
    @WithMockUser(roles = "USER")
    void getMyOrders_Success() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/orders - 내 주문 목록 조회 실패 (인증 없음)")
    void getMyOrders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== 주문 상세 조회 API 테스트 (인증 필요) ==========

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 상세 조회 성공")
    @WithMockUser(roles = "USER")
    void getOrderDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{orderId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalPrice").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 상세 조회 실패 (존재하지 않는 주문)")
    @WithMockUser(roles = "USER")
    void getOrderDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{orderId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - 주문 상세 조회 실패 (인증 없음)")
    void getOrderDetail_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/orders/{orderId}", 1))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
