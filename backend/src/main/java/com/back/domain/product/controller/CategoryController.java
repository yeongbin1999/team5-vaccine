package com.back.domain.product.controller;

import com.back.domain.product.dto.category.CategoryRequestDto;
import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 조회 API (전체 사용자 접근 가능)
    // 특정 카테고리 조회
    @GetMapping("/api/v1/categories/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Integer id) {
        CategoryResponseDto category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // 전체 카테고리 조회
    @GetMapping("/api/v1/categories")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<CategoryResponseDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // 루트 카테고리 조회 (부모 카테고리가 없는 카테고리들)
    @GetMapping("/api/v1/categories/roots")
    public ResponseEntity<List<CategoryResponseDto>> getRootCategories() {
        List<CategoryResponseDto> rootCategories = categoryService.getRootCategories();
        return ResponseEntity.ok(rootCategories);
    }

    //카테고리 관리 API (관리자 전용)
    // 카테고리 생성
    @PostMapping("/api/v1/admin/categories")
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto createdCategory = categoryService.createCategory(requestDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // 카테고리 수정
    @PutMapping("/api/v1/admin/categories/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequestDto requestDto) {
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, requestDto);
        return ResponseEntity.ok(updatedCategory);
    }

    // 카테고리 삭제
    @DeleteMapping("/api/v1/admin/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}