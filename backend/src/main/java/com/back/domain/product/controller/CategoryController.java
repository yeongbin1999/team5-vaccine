package com.back.domain.product.controller;

import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 조회 API (전체 사용자 접근 가능 - 인증 불필요)
    // 특정 카테고리 조회
    @GetMapping("/api/v1/categories/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Integer id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // 전체 카테고리 조회
    @GetMapping("/api/v1/categories")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // 루트 카테고리 조회 (부모 카테고리가 없는 카테고리들)
    @GetMapping("/api/v1/categories/roots")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> rootCategories = categoryService.getRootCategories();
        return ResponseEntity.ok(rootCategories);
    }


}