package com.back.domain.product.controller;

import com.back.domain.product.dto.category.CategoryRequestDto;
import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.service.CategoryService;
import com.back.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //카테고리 관리 API (관리자 전용)
    // 카테고리 생성
    @PostMapping("/api/v1/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @RequestBody CategoryRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 카테고리 생성자 정보 로깅 (필요시) - 테스트 환경에서는 customUserDetails가 null일 수 있음
        if (customUserDetails != null) {
            System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") created a new category: " + requestDto.name());
        }
        
        CategoryResponseDto createdCategory = categoryService.createCategory(requestDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // 카테고리 수정
    @PutMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Integer id, 
            @Valid @RequestBody CategoryRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 카테고리 수정자 정보 로깅 (필요시) - 테스트 환경에서는 customUserDetails가 null일 수 있음
        if (customUserDetails != null) {
            System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") updated category ID: " + id);
        }
        
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, requestDto);
        return ResponseEntity.ok(updatedCategory);
    }

    // 카테고리 삭제
    @DeleteMapping("/api/v1/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        // 카테고리 삭제자 정보 로깅 (필요시) - 테스트 환경에서는 customUserDetails가 null일 수 있음
        if (customUserDetails != null) {
            System.out.println("Admin user: " + customUserDetails.getName() + " (" + customUserDetails.getEmail() + ") deleted category ID: " + id);
        }
        
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}