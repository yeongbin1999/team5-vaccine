package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductRequestDto;
import com.back.domain.product.dto.product.ProductSearchDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProductControllerTest {
    
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

    // ========== 공개 API 테스트 (인증 불필요) ==========

    @Test
    @DisplayName("GET /api/v1/products - 상품 목록 조회 (인증 불필요)")
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - 상품 상세 조회 성공 (인증 불필요)")
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

    // ========== 검색 API 테스트 (인증 불필요) ==========

    @Test
    @DisplayName("POST /api/v1/products/search - 통합 검색 (인증 불필요)")
    void searchProducts_Success() throws Exception {
        ProductSearchDto searchDto = new ProductSearchDto(
                "에티오피아", null, null, null, null, true, false
        );

        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/search - 쿼리 파라미터 검색 (인증 불필요)")
    void searchProductsWithParams_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", "콜롬비아"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/search/name - 상품명 검색 (인증 불필요)")
    void searchProductsByName_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/search/name")
                        .param("name", "브라질"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{categoryId} - 카테고리별 상품 조회 (인증 불필요)")
    void getProductsByCategory_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/{categoryId}", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - 가격 범위 검색 (인증 불필요)")
    void getProductsByPriceRange_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range")
                        .param("minPrice", "16000")
                        .param("maxPrice", "25000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/out-of-stock - 품절 상품 조회 (인증 불필요)")
    void getOutOfStockProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/out-of-stock"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}