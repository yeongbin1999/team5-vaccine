package com.back.domain.product.controller;

import com.back.domain.product.dto.category.CategoryRequestDto;
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
        // 하위 카테고리 생성용 DTO (parentId 1은 data.sql에 '식품'으로 가정)
        testSubCategoryRequestDto = new CategoryRequestDto("새로운 하위 카테고리", 1);
        // 유효성 검증 실패 테스트용 DTO (이름 누락)
        invalidCategoryRequestDto = new CategoryRequestDto("", null);
    }

    // 2-2.1 카테고리 목록 조회(최상위 카테고리)
    @Test
    @DisplayName("GET /api/v1/categories/roots - 최상위 카테고리 목록 조회")
    void getRootCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/roots"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == '식품')]").exists())
                .andExpect(jsonPath("$[?(@.name == '음료')]").exists());
    }

    // 2-2.2 모든 카테고리 목록 조회
    @Test
    @DisplayName("GET /api/v1/categories - 모든 카테고리 목록 조회")
    void getAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)) // data.sql에 5개 카테고리 있음
                .andExpect(jsonPath("$[?(@.name == '식품')]").exists())
                .andExpect(jsonPath("$[?(@.name == '커피빈')]").exists())
                .andExpect(jsonPath("$[?(@.name == '음료')]").exists())
                .andExpect(jsonPath("$[?(@.name == '주스')]").exists())
                .andExpect(jsonPath("$[?(@.name == '차')]").exists());
    }

    // 2-2.3 카테고리 상세 조회
    @Test
    @DisplayName("GET /api/v1/categories/{categoryId} - 카테고리 상세 조회 성공")
    void getCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @DisplayName("GET /api/v1/categories/{categoryId} - 카테고리 상세 조회 실패 (카테고리 없음)")
    void getCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 2-2.4 관리자 - 카테고리 추가
    @Test
    @DisplayName("POST /api/v1/admin/categories - 최상위 카테고리 추가 성공")
    @Transactional
    void createRootCategory_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("새로운 최상위 카테고리"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 하위 카테고리 추가 성공")
    @Transactional
    void createSubCategory_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSubCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("새로운 하위 카테고리"))
                .andExpect(jsonPath("$.parentId").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 카테고리 추가 실패 (필수 필드 누락)")
    void createCategory_BadRequest_MissingField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 카테고리 추가 실패 (부모 카테고리 없음)")
    void createCategory_BadRequest_ParentNotFound() throws Exception {
        CategoryRequestDto dtoWithInvalidParent = new CategoryRequestDto("유효하지 않은 부모 카테고리", 9999);
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithInvalidParent)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 2-2.5 관리자 - 카테고리 수정
    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 성공")
    @Transactional
    void updateCategory_Success() throws Exception {
        CategoryRequestDto updateDto = new CategoryRequestDto("수정된 식품", null);
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("수정된 식품"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 실패 (카테고리 없음)")
    void updateCategory_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 실패 (유효성 검증 실패)")
    void updateCategory_BadRequest_Validation() throws Exception {
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // 2-2.6 관리자 - 카테고리 삭제
    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 성공")
    @Transactional
    void deleteCategory_Success() throws Exception {
        // 새로운 카테고리를 먼저 생성하고 삭제하는 방식으로 테스트
        CategoryRequestDto newCategory = new CategoryRequestDto("삭제 테스트 카테고리", null);
        
        String response = mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // ObjectMapper를 사용하여 안전하게 ID 추출
        var responseNode = objectMapper.readTree(response);
        int createdId = responseNode.get("id").asInt();
        
        // 생성된 카테고리 삭제 - HTTP Status 204 No Content 응답 확인
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", createdId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/api/v1/categories/{categoryId}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 실패 (카테고리 없음)")
    void deleteCategory_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
