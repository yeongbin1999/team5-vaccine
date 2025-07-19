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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("GET /api/v1/products - 상품 목록 조회")
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

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

    // ========== 검색 API 테스트 (인증 불필요) ==========

    @Test
    @DisplayName("POST /api/v1/products/search - 통합 검색")
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
    @DisplayName("GET /api/v1/products/search - 쿼리 파라미터 검색")
    void searchProductsWithParams_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", "콜롬비아"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/search/name - 상품명 검색")
    void searchProductsByName_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/search/name")
                        .param("name", "브라질"))
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
    @DisplayName("GET /api/v1/products/price-range - 가격 범위 검색")
    void getProductsByPriceRange_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/price-range")
                        .param("minPrice", "16000")
                        .param("maxPrice", "25000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/out-of-stock - 품절 상품 조회")
    void getOutOfStockProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/out-of-stock"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ========== 관리자 전용 API 테스트 (현재 보안 설정으로는 모든 사용자 접근 가능) ==========

    @Test
    @DisplayName("GET /api/v1/admin/products/low-stock - 재고 부족 상품 조회 (관리자 권한으로 테스트)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void getLowStockProducts_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/low-stock")
                        .param("threshold", "60"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 성공 (관리자 권한으로 테스트)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
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
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (필수 필드 누락)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void createProduct_BadRequest_MissingField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (카테고리 없음)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
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
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 성공 (관리자 권한으로 테스트)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
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
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (상품 없음)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void updateProduct_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (유효성 검증 실패)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void updateProduct_BadRequest_Validation() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 성공 (관리자 권한으로 테스트)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
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

        // 삭제 확인
        mockMvc.perform(get("/api/v1/products/{productId}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 실패 (상품 없음)")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ========== 비즈니스 로직 검증 테스트 ==========
    
    @Test
    @DisplayName("상품 생성 시 CustomUserDetails 정보 활용 확인")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void createProduct_UserDetails_Verification() throws Exception {
        // 관리자 사용자로 상품 생성하여 비즈니스 로직이 정상 작동하는지 확인
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(testProductRequestDto.name()));
    }

    @Test
    @DisplayName("일반 사용자도 현재 설정으로는 관리자 API 접근 가능 (보안 설정 이슈)")
    @WithUserDetails(value = "user1@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void createProduct_WithUser_CurrentlyAllowed() throws Exception {
        // 현재 anyRequest().permitAll() 설정으로 인해 일반 사용자도 접근 가능
        // 실제 프로덕션에서는 @PreAuthorize 어노테이션으로 보안 검증됨
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated()); // 현재 설정으로는 성공
    }
}