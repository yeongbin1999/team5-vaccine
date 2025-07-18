package com.back.domain.user.dto;

public record UserResponse(
        Integer id,
        String name,
        String email,
        String phone,
        String address) {
}
