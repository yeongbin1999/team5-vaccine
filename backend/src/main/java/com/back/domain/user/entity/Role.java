package com.back.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;        // 스프링 시큐리티에서 사용되는 실제 권한명
    private final String description; // 권한 설명
}
