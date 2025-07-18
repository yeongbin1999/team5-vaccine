package com.back.domain.cart.controller;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.UpdateCartItemRequest;
import com.fasterxml.jackson.databind.JsonNode; // JsonNode import 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult; // MvcResult import 추가
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest // 전체 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc // MockMvc 자동 구성
@ActiveProfiles("test") // 테스트 전용 application-test.yml, schema-test.sql, data-test.sql 활성화
@Transactional // 각 테스트 메서드 후 데이터베이스 변경사항 롤백
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("장바구니 조회 - 초기 데이터 기반")
    void getCart() throws Exception {
        // data-test.sql에 '유저1' (ID: 2)의 장바구니 (ID: 1)에
        // 에티오피아 예가체프 (productId: 1, quantity: 1)와
        // 콜롬비아 수프리모 (productId: 2, quantity: 1)가 미리 추가되어 있다고 가정합니다.
        // 총 수량 2개, 총 가격 18000 + 17000 = 35000원.

        Integer userId = 2; // data-test.sql에 설정된 유저1의 ID

        mockMvc.perform(get("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print()) // 응답 내용 확인을 위해 항상 print() 추가 권장
                .andExpect(jsonPath("$.cartId").value(1)) // data-test.sql의 cart_id
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(35000))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("에티오피아 예가체프"))
                .andExpect(jsonPath("$.items[1].productName").value("콜롬비아 수프리모"));
    }

    @Test
    @DisplayName("장바구니에 새 상품 추가")
    void addItem() throws Exception {
        // data-test.sql에 '유저1' (ID: 2)의 장바구니가 이미 있고, 상품 2개가 존재합니다.
        // 여기에 '브라질 산토스' (productId: 3) 2개를 추가하여 총 4개, 총 가격이 되도록 테스트합니다.
        Integer userId = 2;
        AddCartItemRequest request = new AddCartItemRequest(3, 2); // productId 3, quantity 2

        mockMvc.perform(post("/api/cart/items")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        // 추가 후, 장바구니를 다시 조회하여 변경된 상태를 검증합니다.
        // 기존 2개 + 새로 추가된 2개 = 총 4개
        // 총 가격: 18000(예가체프) + 17000(수프리모) + (16000 * 2)(산토스) = 35000 + 32000 = 67000
        mockMvc.perform(get("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(4))
                .andExpect(jsonPath("$.totalPrice").value(67000));
        // items 배열의 순서는 보장되지 않을 수 있으므로, 특정 인덱스보다 값의 존재 여부나 필터링으로 검증하는 것이 좋습니다.
        // 예를 들어: .andExpect(jsonPath("$.items[?(@.productId == 3)].quantity").value(2));
    }

    @Test
    @DisplayName("장바구니 항목 수량 수정")
    void updateItemQuantity() throws Exception {
        // data-test.sql에 유저1의 장바구니에 cart_item ID 1 (productId 1, quantity 1)이 있습니다.
        // 이를 5개로 수정하는 시나리오입니다.
        Integer userId = 2; // 유저1의 ID
        Integer cartItemIdToUpdate = 1; // data-test.sql에 있는 에티오피아 예가체프의 cart_item ID

        UpdateCartItemRequest request = new UpdateCartItemRequest(5); // 수량을 5로 변경

        mockMvc.perform(put("/api/cart/items/{cartItemId}", cartItemIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        // 수정 후, 장바구니를 조회하여 수량이 변경되었는지 검증합니다.
        // 기존: 예가체프 1개(ID 1), 수프리모 1개(ID 2) -> totalQuantity 2, totalPrice 35000
        // 변경 후: 예가체프 5개(ID 1), 수프리모 1개(ID 2) -> totalQuantity 6, totalPrice (18000*5) + 17000 = 107000
        mockMvc.perform(get("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.items[?(@.id == 1)].quantity").value(5)) // ID 1의 수량 확인
                .andExpect(jsonPath("$.totalQuantity").value(6))
                .andExpect(jsonPath("$.totalPrice").value(107000));
    }

    @Test
    @DisplayName("장바구니 항목 삭제")
    void deleteItem() throws Exception {
        // data-test.sql에 유저1의 장바구니에 cart_item ID 1 (productId 1, quantity 1)이 있습니다.
        // 이 항목을 삭제하는 시나리오입니다.
        Integer userId = 2; // 유저1의 ID
        Integer cartItemIdToDelete = 1; // data-test.sql에 있는 에티오피아 예가체프의 cart_item ID

        mockMvc.perform(delete("/api/cart/items/{cartItemId}", cartItemIdToDelete))
                .andExpect(status().isOk())
                .andDo(print());

        // 삭제 후, 장바구니를 조회하여 항목이 사라졌는지 검증합니다.
        // 기존: 예가체프 1개, 수프리모 1개
        // 삭제 후: 수프리모 1개만 남음 -> totalQuantity 1, totalPrice 17000
        mockMvc.perform(get("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(1))
                .andExpect(jsonPath("$.totalPrice").value(17000))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("콜롬비아 수프리모")); // 남은 항목 확인
    }

    @Test
    @DisplayName("장바구니 비우기")
    void clearCart() throws Exception {
        // data-test.sql에 유저1의 장바구니에 여러 항목이 있다고 가정합니다. (2개 항목)
        // 이 장바구니를 비우는 시나리오입니다.
        Integer userId = 2; // 유저1의 ID

        mockMvc.perform(delete("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print());

        // 비운 후, 장바구니를 조회하여 비어있는지 검증합니다.
        mockMvc.perform(get("/api/cart")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.items").isEmpty()); // 항목 배열이 비어있어야 함
    }
}