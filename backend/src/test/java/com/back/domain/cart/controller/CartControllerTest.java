package com.back.domain.cart.controller;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.CartDto;
import com.back.domain.cart.dto.CartItemDto;
import com.back.domain.cart.dto.UpdateCartItemRequest;
import com.back.domain.cart.service.CartService;
import com.back.global.config.JpaAuditingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CartController.class,
        excludeFilters = {
                // JpaAuditingConfig 클래스 자체를 스캔 대상에서 제외합니다.
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaAuditingConfig.class)
        })
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("장바구니 조회")
    void getCart() throws Exception {
        Integer userId = 1;
        CartDto mockCart = new CartDto(1, 3, 15000,
                List.of(new CartItemDto(1, 10, "아메리카노", 3, 5000))
        );

        Mockito.when(cartService.getCart(userId)).thenReturn(mockCart);

        mockMvc.perform(get("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(15000))
                .andExpect(jsonPath("$.items[0].productName").value("아메리카노"));
    }

    @Test
    @DisplayName("장바구니에 상품 추가")
    void addItem() throws Exception {
        Integer userId = 1;
        AddCartItemRequest request = new AddCartItemRequest(10, 2); // productId 10, quantity 2

        mockMvc.perform(post("/api/cart/items")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(cartService).addItem(eq(userId), any(AddCartItemRequest.class));
    }

    @Test
    @DisplayName("장바구니 항목 수량 수정")
    void updateItemQuantity() throws Exception {
        Integer cartItemId = 1;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(cartService).updateItemQuantity(cartItemId, 5);
    }

    @Test
    @DisplayName("장바구니 항목 삭제")
    void deleteItem() throws Exception {
        Integer cartItemId = 1;

        mockMvc.perform(delete("/api/cart/items/{cartItemId}", cartItemId))
                .andExpect(status().isOk());

        Mockito.verify(cartService).deleteItem(cartItemId);
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() throws Exception {
        Integer userId = 1;

        mockMvc.perform(delete("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk());

        Mockito.verify(cartService).clearCart(userId);
    }
}