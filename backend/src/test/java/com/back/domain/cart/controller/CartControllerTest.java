package com.back.domain.cart.controller;

import com.back.domain.cart.dto.AddCartItemRequest;
import com.back.domain.cart.dto.CartDto;
import com.back.domain.cart.dto.UpdateCartItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "USER")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 특정 userId의 장바구니에서 특정 productId를 가진 cartItemId를 찾는 헬퍼 메서드.
     * AUTO_INCREMENT 환경에서 cartItemId를 동적으로 얻기 위해 사용됩니다.
     *
     * @param userId 장바구니를 조회할 사용자 ID
     * @param productId 찾을 상품 ID
     * @return 찾은 cartItemId, 없으면 RuntimeException (테스트 설정 오류로 간주)
     * @throws Exception MockMvc perform 실패 시 예외
     */
    private Integer getCartItemIdByProductId(Integer userId, Integer productId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        CartDto cartDto = objectMapper.readValue(jsonResponse, CartDto.class);

        return cartDto.items().stream()
                .filter(item -> item.productId().equals(productId))
                .map(item -> item.id())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Test setup error: CartItem not found for userId " + userId + " and productId " + productId));
    }

    // --- getCart 테스트 ---
    @Test
    @DisplayName("장바구니 조회 - 성공적인 조회 및 초기 데이터 검증")
    void getCart_Success() throws Exception {
        Integer userId = 2;
        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.cartId").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(35000))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("에티오피아 예가체프"))
                .andExpect(jsonPath("$.items[1].productName").value("콜롬비아 수프리모"));
    }

    @Test
    @DisplayName("장바구니 조회 - 장바구니가 없는 사용자 (새 장바구니 생성 확인)")
    void getCart_UserWithoutCart_CreatesNewCart() throws Exception {
        Integer userId = 5;

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk()) // 장바구니 생성 후 200 OK 반환
                .andDo(print())
                .andExpect(jsonPath("$.cartId").isNumber())
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("장바구니 조회 - 존재하지 않는 사용자 ID")
    void getCart_NonExistentUser() throws Exception {
        Integer nonExistentUserId = 999;

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(nonExistentUserId)))
                .andExpect(status().isNotFound()) // NoSuchElementException -> 404 Not Found
                .andDo(print())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다. userId: " + nonExistentUserId));
    }

    // --- addItem 테스트 ---
    @Test
    @DisplayName("장바구니에 새 상품 추가 - 성공")
    void addItem_NewProduct_Success() throws Exception {
        Integer userId = 2;
        Integer productIdToAdd = 3;
        Integer quantityToAdd = 2;

        AddCartItemRequest request = new AddCartItemRequest(productIdToAdd, quantityToAdd);

        mockMvc.perform(post("/api/v1/carts/items")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(4))
                .andExpect(jsonPath("$.totalPrice").value(67000))
                .andExpect(jsonPath("$.items[?(@.productId == " + productIdToAdd + ")].quantity").value(quantityToAdd));
    }

    @Test
    @DisplayName("장바구니에 이미 있는 상품 추가 시 수량 업데이트 - 성공")
    void addItem_ExistingProduct_UpdatesQuantity_Success() throws Exception {
        Integer userId = 2;
        Integer productIdToUpdate = 1;
        Integer quantityToAdd = 3;

        AddCartItemRequest request = new AddCartItemRequest(productIdToUpdate, quantityToAdd);

        mockMvc.perform(post("/api/v1/carts/items")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        Integer updatedCartItemId = getCartItemIdByProductId(userId, productIdToUpdate);
        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.items[?(@.id == " + updatedCartItemId + ")].quantity").value(4))
                .andExpect(jsonPath("$.totalQuantity").value(5))
                .andExpect(jsonPath("$.totalPrice").value(89000));
    }

    @Test
    @DisplayName("장바구니에 상품 추가 - 존재하지 않는 상품 ID")
    void addItem_NonExistentProduct() throws Exception {
        Integer userId = 2;
        Integer nonExistentProductId = 999;
        AddCartItemRequest request = new AddCartItemRequest(nonExistentProductId, 1);

        mockMvc.perform(post("/api/v1/carts/items")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // NoSuchElementException -> 404 Not Found
                .andDo(print())
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다. productId: " + nonExistentProductId));
    }

    // --- updateItemQuantity 테스트 ---
    @Test
    @DisplayName("장바구니 항목 수량 수정 - 성공")
    void updateItemQuantity_Success() throws Exception {
        Integer userId = 2;
        Integer productIdToUpdate = 1;
        Integer cartItemIdToUpdate = getCartItemIdByProductId(userId, productIdToUpdate);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        mockMvc.perform(put("/api/v1/carts/items/{cartItemId}", cartItemIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.items[?(@.id == " + cartItemIdToUpdate + ")].quantity").value(5))
                .andExpect(jsonPath("$.totalQuantity").value(6))
                .andExpect(jsonPath("$.totalPrice").value(107000));
    }

    @Test
    @DisplayName("장바구니 항목 수량 수정 - 존재하지 않는 cartItemId")
    void updateItemQuantity_NonExistentCartItem() throws Exception {
        Integer nonExistentCartItemId = 999;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        mockMvc.perform(put("/api/v1/carts/items/{cartItemId}", nonExistentCartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()) // NoSuchElementException -> 404 Not Found
                .andDo(print())
                .andExpect(jsonPath("$.message").value("장바구니 항목을 찾을 수 없습니다. cartItemId: " + nonExistentCartItemId));
    }

    @Test
    @DisplayName("장바구니 항목 수량 수정 - 수량을 0으로 설정 시도")
    void updateItemQuantity_SetToZero() throws Exception {
        Integer userId = 2;
        Integer productIdToUpdate = 1; // 에티오피아 예가체프
        Integer cartItemIdToUpdate = getCartItemIdByProductId(userId, productIdToUpdate);

        UpdateCartItemRequest request = new UpdateCartItemRequest(0);

        mockMvc.perform(put("/api/v1/carts/items/{cartItemId}", cartItemIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // 예상: 200 OK (수량이 0으로 업데이트되거나 삭제됨)
                .andDo(print());

        // 수량 0으로 업데이트 시 해당 항목이 삭제되므로, 해당 항목이 존재하지 않는지 검증합니다.
        // 그리고 장바구니의 총 수량과 총 가격이 올바르게 갱신되었는지 확인합니다.
        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.items[?(@.id == " + cartItemIdToUpdate + ")]").doesNotExist()) // 해당 항목이 없는지 검증
                .andExpect(jsonPath("$.totalQuantity").value(1)) // DTO가 계산한 값 검증
                .andExpect(jsonPath("$.totalPrice").value(17000)); // DTO가 계산한 값 검증
    }

    @Test
    @DisplayName("장바구니 항목 수량 수정 - 수량을 음수로 설정 시도")
    void updateItemQuantity_SetToNegative() throws Exception {
        Integer userId = 2;
        Integer productIdToUpdate = 1;
        Integer cartItemIdToUpdate = getCartItemIdByProductId(userId, productIdToUpdate);

        UpdateCartItemRequest request = new UpdateCartItemRequest(-1);

        mockMvc.perform(put("/api/v1/carts/items/{cartItemId}", cartItemIdToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // DTO @Min 유효성 검사 (Spring 기본 처리)
                .andDo(print());
    }

    // --- deleteItem 테스트 ---
    @Test
    @DisplayName("장바구니 항목 삭제 - 성공적인 삭제 (다수 항목 중 하나)")
    void deleteItem_MultipleItems_Success() throws Exception {
        Integer userId = 4;
        Integer productIdToDelete = 2;
        Integer cartItemIdToDelete = getCartItemIdByProductId(userId, productIdToDelete);

        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", cartItemIdToDelete))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(4))
                .andExpect(jsonPath("$.totalPrice").value(75000))
                .andExpect(jsonPath("$.items[?(@.id == " + cartItemIdToDelete + ")]").doesNotExist())
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    @DisplayName("장바구니 항목 삭제 - 단일 항목 삭제 후 장바구니 비워짐")
    void deleteItem_SingleItem_EmptiesCart() throws Exception {
        Integer userId = 3;
        Integer productIdToDelete = 3;
        Integer cartItemIdToDelete = getCartItemIdByProductId(userId, productIdToDelete);

        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", cartItemIdToDelete))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    @DisplayName("장바구니 항목 삭제 - 존재하지 않는 cartItemId")
    void deleteItem_NonExistentCartItem() throws Exception {
        Integer nonExistentCartItemId = 999;

        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", nonExistentCartItemId))
                .andExpect(status().isNotFound()) // NoSuchElementException -> 404 Not Found
                .andDo(print())
                .andExpect(jsonPath("$.message").value("장바구니 항목을 찾을 수 없습니다. ID: " + nonExistentCartItemId));
    }

    @Test
    @DisplayName("장바구니 항목 삭제 - 유효하지 않은 ID 형식 (음수)")
    void deleteItem_InvalidIdFormatNegative() throws Exception {
        Integer invalidCartItemId = -1;

        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", invalidCartItemId))
                .andExpect(status().isBadRequest()) // @Positive 유효성 검사 (Spring 기본 처리)
                .andDo(print());
    }

    @Test
    @DisplayName("장바구니 항목 삭제 - 유효하지 않은 ID 형식 (0)")
    void deleteItem_InvalidIdFormatZero() throws Exception {
        Integer invalidCartItemId = 0;

        mockMvc.perform(delete("/api/v1/carts/items/{cartItemId}", invalidCartItemId))
                .andExpect(status().isBadRequest()) // @Positive 유효성 검사 (Spring 기본 처리)
                .andDo(print());
    }

    // --- 보안 관련 테스트 (서비스/컨트롤러 로직 변경 필수) ---
    @Test
    @DisplayName("보안: 다른 유저의 장바구니 조회 시도 (현재 구현에서는 허용될 수 있음)")
    void getCart_AnotherUserCartAccessAttempt() throws Exception {
        Integer targetUserId = 3;

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(targetUserId)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("보안: 다른 유저의 장바구니 항목 수정 시도 (현재 구현에서는 허용될 수 있음)")
    void updateItemQuantity_AnotherUserItemAttempt() throws Exception {
        Integer anotherUserUserId = 3;
        Integer anotherUserProductId = 3;
        Integer anotherUserCartItemId = getCartItemIdByProductId(anotherUserUserId, anotherUserProductId);

        UpdateCartItemRequest request = new UpdateCartItemRequest(10);

        mockMvc.perform(put("/api/v1/carts/items/{cartItemId}", anotherUserCartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("보안: 다른 유저의 장바구니 비우기 시도 (현재 구현에서는 허용될 수 있음)")
    void clearCart_AnotherUserCartAttempt() throws Exception {
        Integer targetUserId = 3;

        mockMvc.perform(delete("/api/v1/carts")
                        .param("userId", String.valueOf(targetUserId)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(targetUserId)))
                .andExpect(jsonPath("$.totalQuantity").value(0));
    }

    // --- clearCart 테스트 ---
    @Test
    @DisplayName("장바구니 전체 비우기 - 성공")
    void clearCart_Success() throws Exception {
        Integer userId = 2;

        mockMvc.perform(delete("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/api/v1/carts")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalQuantity").value(0))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.items").isEmpty());
    }
}
