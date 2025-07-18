package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequestDto testProductRequestDto;
    private ProductRequestDto invalidProductRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 생성 요청 DTO (categoryId 2는 data.sql에 '커피빈'으로 가정)
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

    // 2-1.1 상품 목록 조회
    @Test
    @DisplayName("GET /api/v1/products - 상품 목록 조회")
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // TODO: 향후 구현 예정 - 페이징, 검색, 필터링 기능
    // GET /api/v1/products?page=0&size=10&category=123&search=아메리카노

    // 2-1.2 상품 상세 조회
    @Test
    @DisplayName("GET /api/v1/products/{productId} - 상품 상세 조회 성공")
    void getProductById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - 상품 상세 조회 실패 (상품 없음)")
    void getProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 2-1.3 관리자 - 상품 추가
    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 성공")
    @Transactional
    void createProduct_Success() throws Exception {
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
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (필수 필드 누락)")
    void createProduct_BadRequest_MissingField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (카테고리 없음)")
    void createProduct_BadRequest_CategoryNotFound() throws Exception {
        ProductRequestDto dtoWithInvalidCategory = new ProductRequestDto(
                "유효하지 않은 카테고리 상품", "url", 100, 10, "desc", 9999
        );
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithInvalidCategory)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 2-1.4 관리자 - 상품 수정
    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 성공")
    @Transactional
    void updateProduct_Success() throws Exception {
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
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (상품 없음)")
    void updateProduct_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (유효성 검증 실패)")
    void updateProduct_BadRequest_Validation() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // 2-1.5 관리자 - 상품 삭제
    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 성공")
    @Transactional
    void deleteProduct_Success() throws Exception {
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

        // 삭제 확인
        mockMvc.perform(get("/api/v1/products/{productId}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 실패 (상품 없음)")
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
