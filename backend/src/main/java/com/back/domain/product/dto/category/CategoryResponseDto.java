package com.back.domain.product.dto.category;

import com.back.domain.product.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record CategoryResponseDto(
        Integer id,
        String name,
        Integer parentId,
        List<CategoryResponseDto> children
) {
    public static CategoryResponseDto from(Category category) {
        // 카테고리의 부모 ID를 가져오고, 없으면 null로 설정
        Integer parentId = Optional.ofNullable(category.getParent())
                .map(Category::getId)
                .orElse(null);
        // 자식 카테고리들을 CategoryResponseDto로 변환
        List<CategoryResponseDto> children = category.getChildren().stream()
                .map(CategoryResponseDto::from)
                .collect(Collectors.toList());

        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                parentId,
                children
        );
    }
}