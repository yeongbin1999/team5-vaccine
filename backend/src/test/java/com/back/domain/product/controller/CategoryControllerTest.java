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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // 하위 카테고리 생성용 DTO (parentId 1은 data-test.sql에 '식품'으로 가정)
        testSubCategoryRequestDto = new CategoryRequestDto("새로운 하위 카테고리", 1);
        // 유효성 검증 실패 테스트용 DTO (이름 누락)
        invalidCategoryRequestDto = new CategoryRequestDto("", null);
    }

    // --- 카테고리 조회 API 테스트 ---

    @Test
    @DisplayName("GET /api/v1/categories/roots - 최상위 카테고리 목록 조회 성공")
    void getAllRootCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/roots"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("식품")) // ID 1번 '식품'이 최상위
                .andExpect(jsonPath("$[1].name").value("음료")); // ID 3번 '음료'가 최상위
    }

    @Test
    @DisplayName("GET /api/v1/categories - 모든 카테고리 목록 조회 성공")
    void getAllCategories_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)) // data-test.sql에 5개 카테고리 삽입 가정
                .andExpect(jsonPath("$[0].name").value("식품"))
                .andExpect(jsonPath("$[1].name").value("커피빈"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{categoryId} - 카테고리 상세 조회 성공")
    void getCategoryById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 2)) // ID 2번 '커피빈' 조회
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("커피빈"))
                .andExpect(jsonPath("$.parentId").value(1)); // 부모 ID 확인
    }

    @Test
    @DisplayName("GET /api/v1/categories/{categoryId} - 카테고리 상세 조회 실패 (카테고리 없음)")
    void getCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // --- 관리자 - 카테고리 추가 API 테스트 ---

    @Test
    @DisplayName("POST /api/v1/admin/categories - 최상위 카테고리 추가 성공")
    void createRootCategory_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("새로운 최상위 카테고리"))
                .andExpect(jsonPath("$.parentId").doesNotExist()); // parentId가 없어야 함
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 하위 카테고리 추가 성공")
    void createSubCategory_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testSubCategoryRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("새로운 하위 카테고리"))
                .andExpect(jsonPath("$.parentId").value(1)); // 부모 ID 확인
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
                .andExpect(status().isNotFound()); // CategoryNotFoundException으로 인한 404
    }

    // --- 관리자 - 카테고리 수정 API 테스트 ---

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{categoryId} - 카테고리 수정 성공")
    void updateCategory_Success() throws Exception {
        CategoryRequestDto updateDto = new CategoryRequestDto("수정된 커피빈", 1); // ID 2번 '커피빈' 수정
        mockMvc.perform(put("/api/v1/admin/categories/{categoryId}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("수정된 커피빈"))
                .andExpect(jsonPath("$.parentId").value(1));
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

    // --- 관리자 - 카테고리 삭제 API 테스트 ---

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 성공 (하위 카테고리 없음)")
    void deleteCategory_Success() throws Exception {
        // data-test.sql에 있는 4번 주스 카테고리 삭제 (하위 카테고리 없는 경우)
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 4))
                .andDo(print())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categories/{categoryId}", 4))
                .andExpect(status().isNotFound());
    }

    // 주의: Product가 Category에 외래 키 제약이 있으므로,
    // 상품이 연결된 카테고리를 삭제하려고 하면 `ConstraintViolationException`이 발생할 수 있습니다.
    // 이는 @DeleteMapping에서 적절히 처리해야 합니다.
    // 현재 Product의 FK는 ON DELETE RESTRICT이므로, 연결된 상품이 있으면 삭제가 안됩니다.
    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 실패 (연결된 상품 존재)")
    void deleteCategory_Conflict_ProductsExist() throws Exception {
        // ID 2번 '커피빈' 카테고리는 상품(1,2,3,4)과 연결되어 있음
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 2))
                .andDo(print())
                .andExpect(status().isConflict()); // 또는 400 Bad Request 등 적절한 상태 코드 (구현에 따라 다름)
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{categoryId} - 카테고리 삭제 실패 (카테고리 없음)")
    void deleteCategory_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/{categoryId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}