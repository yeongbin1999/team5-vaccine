package com.back.domain.product.controller;

import com.back.domain.product.dto.category.CategoryRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequestDto testCategoryRequestDto;
    private CategoryRequestDto testSubCategoryRequestDto;
    private CategoryRequestDto invalidCategoryRequestDto;

    @BeforeEach
    void setUp() {
        // 최상위 카테고리 생성용 DTO (parentId = null)
        testCategoryRequestDto = new CategoryRequestDto("새로운 최상위 카테고리", null);
        // 하위 카테고리 생성용 DTO (parentId 1은 data-test.sql에 '식품')
        testSubCategoryRequestDto = new CategoryRequestDto("새로운 하위 카테고리", 1);
        // 유효성 검증 실패 테스트용 DTO (이름 누락)
        invalidCategoryRequestDto = new CategoryRequestDto("", null);
    }

    // ========== 공개 API 테스트 (인증 불필요) ==========

    @Test
    @DisplayName("GET /api/v1/categories/roots - 최상위 카테고리 목록 조회 (인증 불필요)")
    @WithMockUser
    void getRootCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/roots"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/categories - 모든 카테고리 목록 조회 (인증 불필요)")
    @WithMockUser
    void getAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/categories/{categoryId} - 카테고리 상세 조회 성공 (인증 불필요)")
    @WithMockUser
    void getCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @DisplayName("GET /api/v1/categories/{categoryId} - 카테고리 상세 조회 실패 (카테고리 없음)")
    @WithMockUser
    void getCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
