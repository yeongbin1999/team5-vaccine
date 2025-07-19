package com.back.global.security.jwt;

public record JwtTokens(
        String accessToken,
        String refreshToken
) {}
