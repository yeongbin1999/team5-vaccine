package com.back.domain.product.dto.product;

import jakarta.validation.constraints.Min;

public record ProductSearchDto(
        String name,
        Integer categoryId,
        
        @Min(value = 0, message = "최소 가격은 0 이상이어야 합니다.")
        Integer minPrice,

        @Min(value = 0, message = "최대 가격은 0 이상이어야 합니다.")
        Integer maxPrice,
        
        @Min(value = 0, message = "최소 재고는 0 이상이어야 합니다.")
        Integer minStock,
        Boolean includeOutOfStock, // 품절 상품 포함 여부 (기본값: true)
        Boolean includeSubCategories // 하위 카테고리 포함 여부 (기본값: false)
) {
    
    // 기본값 설정을 위한 생성자
    public ProductSearchDto {
        if (includeOutOfStock == null) {
            includeOutOfStock = true;
        }
        if (includeSubCategories == null) {
            includeSubCategories = false;
        }
    }
    
    // 검색 조건이 있는지 확인하는 메서드
    public boolean hasSearchConditions() {
        return name != null || categoryId != null || minPrice != null || 
               maxPrice != null || minStock != null || !includeOutOfStock;
    }
    
    // 이름 검색 조건이 있는지 확인
    public boolean hasNameCondition() {
        return name != null && !name.trim().isEmpty();
    }
    
    // 카테고리 검색 조건이 있는지 확인
    public boolean hasCategoryCondition() {
        return categoryId != null;
    }
    
    // 가격 범위 검색 조건이 있는지 확인
    public boolean hasPriceRangeCondition() {
        return minPrice != null || maxPrice != null;
    }
    
    // 재고 검색 조건이 있는지 확인
    public boolean hasStockCondition() {
        return minStock != null || !includeOutOfStock;
    }
}