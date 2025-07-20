package com.back.domain.admin.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
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
public class AdminProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequestDto testProductRequestDto;
    private ProductRequestDto invalidProductRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 생성 요청 DTO (categoryId 2는 data-test.sql에 '커피빈')
        testProductRequestDto = new ProductRequestDto(
                "테스트 새 상품",
                "http://test.com/new_product.jpg",
                25000,
                100,
                "테스트용 상품 설명입니다.",
                2
        );

        // 유효성 검증 실패 테스트용 DTO (이름 누락)
        invalidProductRequestDto = new ProductRequestDto(
                "",
                "http://test.com/invalid_image.jpg",
                1000,
                10,
                "유효하지 않은 상품 설명",
                1
        );
    }

    // ========== 관리자 전용 API 테스트 ==========

    @Test
    @DisplayName("GET /api/v1/admin/products/low-stock - 재고 부족 상품 조회 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getLowStockProducts_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/low-stock")
                        .param("threshold", "60"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/admin/products/low-stock - 재고 부족 상품 조회 (권한 없음)")
    @WithMockUser(roles = "USER")
    void getLowStockProducts_Forbidden_WithUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/low-stock"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/products/low-stock - 재고 부족 상품 조회 (인증 없음)")
    void getLowStockProducts_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/low-stock"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void createProduct_Success_WithAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(testProductRequestDto.name()))
                .andExpect(jsonPath("$.price").value(testProductRequestDto.price()));
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void createProduct_Forbidden_WithUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (인증 없음)")
    void createProduct_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (필수 필드 누락)")
    @WithMockUser(roles = "ADMIN")
    void createProduct_BadRequest_MissingField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (카테고리 없음)")
    @WithMockUser(roles = "ADMIN")
    void createProduct_NotFound_CategoryNotFound() throws Exception {
        ProductRequestDto dtoWithInvalidCategory = new ProductRequestDto(
                "유효하지 않은 카테고리 상품", "url", 100, 10, "desc", 9999
        );
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithInvalidCategory)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_Success_WithAdmin() throws Exception {
        ProductRequestDto updateDto = new ProductRequestDto(
                "수정된 상품",
                "http://updated.com/image.jpg",
                20000,
                45,
                "수정된 상품 설명입니다.",
                2
        );

        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("수정된 상품"))
                .andExpect(jsonPath("$.price").value(20000));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void updateProduct_Forbidden_WithUser() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (상품 없음)")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (유효성 검증 실패)")
    @WithMockUser(roles = "ADMIN")
    void updateProduct_BadRequest_Validation() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_Success_WithAdmin() throws Exception {
        // 새로운 상품을 먼저 생성하고 삭제하는 방식으로 테스트
        String response = mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // ObjectMapper를 사용하여 안전하게 ID 추출
        var responseNode = objectMapper.readTree(response);
        int createdId = responseNode.get("id").asInt();
        
        // 생성된 상품 삭제 - HTTP Status 204 No Content 응답 확인
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", createdId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 확인 - 공개 API로 확인
        mockMvc.perform(get("/api/v1/products/{productId}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void deleteProduct_Forbidden_WithUser() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 1))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 실패 (인증 없음)")
    void deleteProduct_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 1))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 실패 (상품 없음)")
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
