package com.back.domain.user.dto;

import com.back.domain.user.entity.User;

public record UserResponse(
        Integer id,
        String name,
        String email,
        String phone,
        String address
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress()
        );
    }
}
