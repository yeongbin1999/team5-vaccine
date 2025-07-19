package com.back.domain.auth.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}

