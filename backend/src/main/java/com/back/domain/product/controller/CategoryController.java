package com.back.domain.product.controller;

import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 조회 API (전체 사용자 접근 가능 - 인증 불필요)
    // 특정 카테고리 조회
    @GetMapping("/api/v1/categories/{id}")
    @Operation(summary = "특정 카테고리 조회",
            description = "카테고리 ID를 통해 특정 카테고리의 상세 정보를 조회합니다. 모든 사용자가 접근 가능합니다.")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Integer id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // 전체 카테고리 조회
    @GetMapping("/api/v1/categories")
    @Operation(summary = "전체 카테고리 목록 조회",
            description = "등록된 모든 카테고리의 목록을 조회합니다. 모든 사용자가 접근 가능합니다.")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // 루트 카테고리 조회 (부모 카테고리가 없는 카테고리들)
    @GetMapping("/api/v1/categories/roots")
    @Operation(summary = "루트 카테고리 조회",
            description = "부모 카테고리가 없는 최상위 카테고리들의 목록을 조회합니다.")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> rootCategories = categoryService.getRootCategories();
        return ResponseEntity.ok(rootCategories);
    }

}
