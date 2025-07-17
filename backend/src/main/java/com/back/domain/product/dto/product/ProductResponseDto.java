package com.back.domain.product.dto.product;

import com.back.domain.product.dto.category.CategoryResponseDto;
import com.back.domain.product.entity.Product;

import java.time.LocalDateTime;
import java.util.Optional;

public record ProductResponseDto(
        Integer id,
        String name,
        String imageUrl,
        Integer price,
        Integer stock,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CategoryResponseDto category
) {

    public static ProductResponseDto from(Product product) {
        // 카테고리가 null일 수 있으므로 Optional을 사용해서 변환
        CategoryResponseDto categoryResponse = Optional.ofNullable(product.getCategory())
                .map(CategoryResponseDto::from)
                .orElse(null);

        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                categoryResponse
        );
    }
}