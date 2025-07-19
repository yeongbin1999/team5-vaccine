package com.back.domain.user.dto;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;

public record UserResponse(
        Integer id,
        String name,
        String email,
        String phone,
        String address,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole()
        );
    }
}
