package com.back.domain.user.dto;

public record UpdateUserRequest(
        String name,
        String phone,
        String address
) {}
