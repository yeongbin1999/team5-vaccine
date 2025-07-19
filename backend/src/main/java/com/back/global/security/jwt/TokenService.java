package com.back.global.security.jwt;

import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;  // 유저 정보 조회용

    // 로그인/최초 발급
    public JwtTokens generateToken(Integer userId, String name, String email, String role) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId, name, email, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // Redis에 userId(key) → refreshToken(value) 저장 (기존 토큰 덮어쓰기)
        refreshTokenRepository.saveRefreshToken(userId, refreshToken, jwtTokenProvider.getRefreshTokenExpiration());

        return new JwtTokens(
                accessToken,
                refreshToken
        );
    }

    // 토큰 재발급 (원자적 토큰 회전)
    public JwtTokens reissue(String refreshToken) {
        // 1. 토큰 유효성 검사 (만료, 위변조 등)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2. refreshToken에서 userId 추출
        Integer userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        // 3. 새 Refresh Token 생성
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 4. Redis에서 원자적 토큰 회전 시도
        boolean rotateSuccess = refreshTokenRepository.rotateRefreshToken(
                userId,
                refreshToken,
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        if (!rotateSuccess) {
            throw new RuntimeException("Refresh token rotation failed");
        }

        // 5. DB에서 유저 정보 조회
        User user = userService.findById(userId);
        String name = user.getName();
        String email = user.getEmail();
        String role = user.getRole().name();

        // 6. Access Token 새로 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, name, email, role);

        return new JwtTokens(
                newAccessToken,
                newRefreshToken
        );
    }

    // 로그아웃
    public void logout(Integer userId) {
        refreshTokenRepository.deleteRefreshToken(userId);
    }

    public Duration getRefreshTokenExpiration() {
        return jwtTokenProvider.getRefreshTokenExpiration();
    }
}
