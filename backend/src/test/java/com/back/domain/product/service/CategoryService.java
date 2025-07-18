package com.back.domain.product.service;

import com.back.domain.product.dto.category.CategoryRequestDto;
import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.entity.Category;
import com.back.domain.product.exception.CategoryNotFoundException;
import com.back.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // 카테고리 생성 메서드
    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        Category parentCategory = null;
        if (requestDto.parentId() != null) {
            parentCategory = categoryRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent Category not found with ID: " + requestDto.parentId()));
        }

        Category category = Category.builder()
                .name(requestDto.name())
                .parent(parentCategory)
                .build();
        Category savedCategory = categoryRepository.save(category);

        return CategoryResponseDto.from(savedCategory);
    }

    // 특정 ID의 카테고리 정보 조회 메서드
    public CategoryResponseDto getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));
        return CategoryResponseDto.from(category);
    }

    // 전체 카테고리 목록 조회 메서드
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }

    // 부모 카테고리가 없는 루트 카테고리 목록 조회 메서드
    public List<CategoryResponseDto> getRootCategories() {
        // 부모 카테고리가 없는 카테고리들만 조회합니다.
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());
    }

    // 특정 ID의 카테고리 업데이트 메서드
    @Transactional
    public CategoryResponseDto updateCategory(Integer id, CategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + id));

        Category newParentCategory = null;
        if (requestDto.parentId() != null) {
            newParentCategory = categoryRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent Category not found with ID: " + requestDto.parentId()));
        }

        category.setName(requestDto.name());
        category.setParent(newParentCategory);


        return CategoryResponseDto.from(category);
    }

    // 카테고리 삭제 메서드
    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }
}