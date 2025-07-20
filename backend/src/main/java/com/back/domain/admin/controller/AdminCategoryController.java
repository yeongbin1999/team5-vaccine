package com.back.domain.admin.controller;

import com.back.domain.product.dto.category.CategoryRequestDto;
import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.service.CategoryService;
import com.back.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    
    private final CategoryService categoryService;

    // 카테고리 생성
    @PostMapping
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
    @PutMapping("/{id}")
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer id
    ) {

        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
