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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc; // 컨트롤러 테스트를 위한 MockMvc 객체

    @Autowired
    private ObjectMapper objectMapper; // Java 객체를 JSON으로 변환하기 위한 객체

    private ProductRequestDto testProductRequestDto;
    private ProductRequestDto invalidProductRequestDto; // 유효성 검증 실패 테스트용

    @BeforeEach // 각 테스트 메서드 실행 전 초기화
    void setUp() {
        // 테스트용 상품 생성 요청 DTO (categoryId 2는 data.sql에 '커피빈'으로 가정)
        testProductRequestDto = new ProductRequestDto(
                "테스트 새 상품",
                "http://test.com/new_product.jpg",
                25000,
                100,
                "테스트용 상품 설명입니다.",
                2 // category_id (data.sql에 존재하는 ID)
        );

        // 유효성 검증 실패 테스트용 DTO (이름 누락)
        invalidProductRequestDto = new ProductRequestDto(
                "", // 이름이 비어있음 (NotBlank 위반)
                "http://test.com/invalid_image.jpg",
                1000,
                10,
                "유효하지 않은 상품 설명",
                1
        );
    }

    // --- 상품 조회 API 테스트 ---

    @Test
    @DisplayName("GET /api/v1/products - 상품 목록 조회 성공")
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andDo(print()) // 요청 및 응답 상세 내용 콘솔 출력 (디버깅용)
                .andExpect(status().isOk()) // HTTP 200 OK
                .andExpect(jsonPath("$").isArray()) // 응답이 배열인지 확인
                .andExpect(jsonPath("$[0].id").exists()) // 첫 번째 상품의 ID 존재 여부 확인
                .andExpect(jsonPath("$[0].name").value("에티오피아 예가체프")); // data.sql 기준 첫 상품 이름 확인
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - 상품 상세 조회 성공")
    void getProductById_Success() throws Exception {
        // data.sql에 ID가 1인 상품이 존재한다고 가정
        mockMvc.perform(get("/api/v1/products/{productId}", 1))
                .andDo(print())
                .andExpect(status().isOk()) // HTTP 200 OK
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("에티오피아 예가체프"))
                .andExpect(jsonPath("$.category").exists()); // 카테고리 객체가 존재하는지만 확인
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - 상품 상세 조회 실패 (상품 없음)")
    void getProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}", 9999)) // 존재하지 않는 ID
                .andDo(print())
                .andExpect(status().isNotFound()); // HTTP 404 Not Found
    }

    // --- 관리자 - 상품 추가 API 테스트 ---

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 성공")
    void createProduct_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON) // 요청 본문 타입 JSON
                        .content(objectMapper.writeValueAsString(testProductRequestDto))) // DTO를 JSON 문자열로 변환
                .andDo(print())
                .andExpect(status().isCreated()) // HTTP 201 Created
                .andExpect(jsonPath("$.id").exists()) // ID가 생성되었는지 확인
                .andExpect(jsonPath("$.name").value(testProductRequestDto.name()))
                .andExpect(jsonPath("$.price").value(testProductRequestDto.price()))
                .andExpect(jsonPath("$.category").exists()); // 카테고리 객체가 존재하는지만 확인
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (필수 필드 누락)")
    void createProduct_BadRequest_MissingField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto))) // 이름이 비어있는 DTO
                .andDo(print())
                .andExpect(status().isBadRequest()); // HTTP 400 Bad Request
    }

    @Test
    @DisplayName("POST /api/v1/admin/products - 상품 추가 실패 (카테고리 없음)")
    void createProduct_BadRequest_CategoryNotFound() throws Exception {
        ProductRequestDto dtoWithInvalidCategory = new ProductRequestDto(
                "유효하지 않은 카테고리 상품", "url", 100, 10, "desc", 9999 // 존재하지 않는 카테고리 ID
        );
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithInvalidCategory)))
                .andDo(print())
                .andExpect(status().isNotFound()); // HTTP 404 Not Found (CategoryNotFoundException)
    }

    // --- 관리자 - 상품 수정 API 테스트 ---

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 성공")
    void updateProduct_Success() throws Exception {
        // data.sql에 ID가 1인 상품을 수정
        ProductRequestDto updateDto = new ProductRequestDto(
                "수정된 예가체프 상품",
                "http://updated.com/image.jpg",
                20000,
                45,
                "수정된 상품 설명입니다.",
                2 // 기존 카테고리 ID 유지
        );

        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk()) // HTTP 200 OK
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("수정된 예가체프 상품"))
                .andExpect(jsonPath("$.price").value(20000));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (상품 없음)")
    void updateProduct_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 9999) // 존재하지 않는 상품 ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProductRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound()); // HTTP 404 Not Found
    }

    @Test
    @DisplayName("PUT /api/v1/admin/products/{productId} - 상품 수정 실패 (유효성 검증 실패)")
    void updateProduct_BadRequest_Validation() throws Exception {
        mockMvc.perform(put("/api/v1/admin/products/{productId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequestDto))) // 이름이 비어있는 DTO
                .andDo(print())
                .andExpect(status().isBadRequest()); // HTTP 400 Bad Request
    }

    // --- 관리자 - 상품 삭제 API 테스트 ---

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 성공")
    void deleteProduct_Success() throws Exception {
        // data.sql에 ID가 5인 상품을 삭제 (과테말라 안티구아 - 주문과 연결되지 않은 상품)
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 5))
                .andDo(print())
                .andExpect(status().isNoContent()); // HTTP 204 No Content

        // 삭제 후 해당 상품이 조회되지 않는지 확인 (선택적 검증)
        mockMvc.perform(get("/api/v1/products/{productId}", 5))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/products/{productId} - 상품 삭제 실패 (상품 없음)")
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/products/{productId}", 9999)) // 존재하지 않는 ID
                .andDo(print())
                .andExpect(status().isNotFound()); // HTTP 404 Not Found
    }
}