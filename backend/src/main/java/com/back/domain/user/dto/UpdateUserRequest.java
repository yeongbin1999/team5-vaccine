package com.back.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "이름은 필수 입력 값이며, 공백으로 둘 수 없습니다.")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name,

        // phone과 address는 현재로서는 NotBlank를 추가하지 않았습니다.
        // 필요에 따라 추가할 수 있습니다.
        String phone,
        String address
) {}
