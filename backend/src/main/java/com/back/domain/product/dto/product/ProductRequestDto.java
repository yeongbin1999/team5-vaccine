package com.back.domain.product.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequestDto (
        @NotBlank(message = "상품 이름을 입력해주세요.")
        @Size(max = 100, message = "상품 이름은 100자를 초과할 수 없습니다.")
        String name,

        String imageUrl,

        @NotNull(message = "가격을 입력해주세요.")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "재고를 입력해주세요.")
        @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        Integer stock,

        @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
        String description,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Integer categoryId
) {}
