package com.back.domain.product.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @NotBlank(message = "카테고리 이름을 작성해주세요.")
        @Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다.")
        String name,

        Integer parentId // 부모 카테고리의 ID// 최상위인 경우는 null이다.
) {}