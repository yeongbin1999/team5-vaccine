package com.back.domain.admin.controller;

import com.back.domain.product.dto.category.CategoryRequestDto;
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
public class AdminCategoryControllerTest {

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

    // ========== 관리자 전용 API 테스트 ==========

    @Test
    @DisplayName("POST /api/v1/admin/categories - 최상위 카테고리 추가 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void createRootCategory_Success_WithAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("새로운 최상위 카테고리"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 하위 카테고리 추가 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void createSubCategory_Success_WithAdmin() throws Exception {
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
    @DisplayName("POST /api/v1/admin/categories - 카테고리 추가 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void createCategory_Forbidden_WithUser() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 카테고리 추가 실패 (인증 없음)")
    void createCategory_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 카테고리 추가 실패 (필수 필드 누락)")
    @WithMockUser(roles = "ADMIN")
    void createCategory_BadRequest_MissingField() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 카테고리 추가 실패 (부모 카테고리 없음)")
    @WithMockUser(roles = "ADMIN")
    void createCategory_NotFound_ParentNotFound() throws Exception {
        CategoryRequestDto dtoWithInvalidParent = new CategoryRequestDto("유효하지 않은 부모 카테고리", 9999);
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithInvalidParent)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_Success_WithAdmin() throws Exception {
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
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void updateCategory_Forbidden_WithUser() throws Exception {
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 실패 (인증 없음)")
    void updateCategory_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 실패 (카테고리 없음)")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 실패 (유효성 검증 실패)")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_BadRequest_Validation() throws Exception {
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_Success_WithAdmin() throws Exception {
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
                .andExpect(status().isOk());

        // 삭제 확인 - 공개 API로 확인
        mockMvc.perform(get("/api/v1/categories/{categoryId}", createdId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void deleteCategory_Forbidden_WithUser() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 1))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 실패 (인증 없음)")
    void deleteCategory_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 1))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 실패 (카테고리 없음)")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
