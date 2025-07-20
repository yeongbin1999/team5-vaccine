package com.back.domain.product.controller;

import com.back.domain.product.dto.product.ProductSearchDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ProductSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/products/search - 상품명 검색")
    void searchProducts_ByName_Success() throws Exception {
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
    @DisplayName("GET /api/v1/products/search - 쿼리 파라미터로 상품명 검색")
    void searchProductsWithParams_ByName_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", "케냐"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/search/name - 상품명으로 검색")
    void searchProductsByName_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/search/name")
                        .param("name", "과테말라"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/category/{categoryId} - 카테고리별 상품 조회")
    void getProductsByCategory_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/category/{categoryId}", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/price-range - 가격 범위로 상품 조회")
    void getProductsByPriceRange_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range")
                        .param("minPrice", "20000")
                        .param("maxPrice", "25000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/products/search - 복합 검색 (상품명 + 카테고리)")
    void searchProducts_ByNameAndCategory_Success() throws Exception {
        ProductSearchDto searchDto = new ProductSearchDto(
                "콜롬비아", 2, null, null, null, true, false
        );

        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/products/search - 대소문자 구분 없는 검색")
    void searchProducts_CaseInsensitive_Success() throws Exception {
        ProductSearchDto searchDto = new ProductSearchDto(
                "ETHIOPIA", null, null, null, null, true, false
        );

        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/products/search - 검색 조건 없음 (전체 조회)")
    void searchProducts_NoCondition_Success() throws Exception {
        ProductSearchDto searchDto = new ProductSearchDto(
                null, null, null, null, null, true, false
        );

        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/products/search - 검색 결과 없음")
    void searchProducts_NoResult_Success() throws Exception {
        ProductSearchDto searchDto = new ProductSearchDto(
                "존재하지않는상품", null, null, null, null, true, false
        );

        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/products/out-of-stock - 품절 상품 조회")
    void getOutOfStockProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/out-of-stock"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

}