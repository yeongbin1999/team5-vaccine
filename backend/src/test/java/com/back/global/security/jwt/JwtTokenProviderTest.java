package com.back.global.security.jwt;

import com.back.global.exception.InvalidTokenException;
import com.back.global.exception.TokenExpiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String secretKey = "my-test-secret-key-my-test-secret-key!!";

    private final Duration accessTokenValidity = Duration.ofMinutes(10);
    private final Duration refreshTokenValidity = Duration.ofDays(7);

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, accessTokenValidity, refreshTokenValidity);
    }

    @Test
    @DisplayName("AccessToken 생성 후 검증 및 UserId 파싱이 가능해야 한다")
    void generateAndParseAccessToken() {
        Integer userId = 123;
        String name = "테스트유저";
        String email = "test@example.com";
        String role = "USER";

        String token = jwtTokenProvider.generateAccessToken(userId, name, email, role);

        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));

        Integer parsedId = jwtTokenProvider.getUserIdFromAccessToken(token);
        assertThat(parsedId).isEqualTo(userId);

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(email);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("RefreshToken 생성 후 검증 및 UserId 파싱이 가능해야 한다")
    void generateAndParseRefreshToken() {
        Integer userId = 456;

        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(refreshToken));

        Integer parsedId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
        assertThat(parsedId).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 InvalidTokenException 예외를 던져야 한다")
    void invalidTokenShouldThrowException() {
        String invalidToken = "this.is.not.jwt";

        assertThrows(InvalidTokenException.class,
                () -> jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("JWT 구조는 맞지만 서명이 다른 경우 InvalidTokenException 발생")
    void tamperedSignatureShouldThrowException() {
        String token = jwtTokenProvider.generateAccessToken(1, "user", "user@test.com", "USER");
        // 토큰의 마지막 문자만 살짝 변경 → 위변조
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(InvalidTokenException.class,
                () -> jwtTokenProvider.validateToken(tampered));
    }

    @Test
    @DisplayName("만료된 AccessToken은 TokenExpiredException 예외를 던져야 한다")
    void expiredAccessTokenShouldThrowException() throws InterruptedException {
        JwtTokenProvider shortLivedProvider =
                new JwtTokenProvider(secretKey, Duration.ofMillis(1000), refreshTokenValidity);

        String token = shortLivedProvider.generateAccessToken(1, "user", "user@example.com", "USER");

        assertDoesNotThrow(() -> shortLivedProvider.validateToken(token));

        Thread.sleep(1200);

        assertThrows(TokenExpiredException.class,
                () -> shortLivedProvider.validateToken(token));
    }

    @Test
    @DisplayName("만료된 RefreshToken은 TokenExpiredException 예외를 던져야 한다")
    void expiredRefreshTokenShouldThrowException() throws InterruptedException {
        JwtTokenProvider shortLivedProvider =
                new JwtTokenProvider(secretKey, accessTokenValidity, Duration.ofMillis(1000));

        String refreshToken = shortLivedProvider.generateRefreshToken(1);

        assertDoesNotThrow(() -> shortLivedProvider.validateToken(refreshToken));

        Thread.sleep(1200);

        assertThrows(TokenExpiredException.class,
                () -> shortLivedProvider.validateToken(refreshToken));
    }

    @Test
    @DisplayName("ADMIN 권한 토큰은 Authentication에 ROLE_ADMIN 권한을 가져야 한다")
    void adminRoleShouldBeParsedCorrectly() {
        Integer adminId = 999;
        String adminName = "관리자";
        String adminEmail = "admin@example.com";
        String adminRole = "ADMIN";

        String token = jwtTokenProvider.generateAccessToken(adminId, adminName, adminEmail, adminRole);

        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(token));

        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        assertThat(authentication.getName()).isEqualTo(adminEmail);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("RefreshToken에는 type=refresh Claim이 포함되어야 한다")
    void refreshTokenShouldContainRefreshType() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(1);

        // type claim 확인
        var claims = jwtTokenProvider.getAllClaims(refreshToken);
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
    }

    @Test
    @DisplayName("필수 Claim 누락된 AccessToken은 getAuthentication 시 예외 발생")
    void missingClaimsShouldThrowWhenCreatingAuthentication() {
        // 이메일, role 없이 minimal한 토큰 생성
        String brokenToken = jwtTokenProvider.generateAccessToken(1, "name-only", null, null);

        assertThrows(NullPointerException.class,
                () -> jwtTokenProvider.getAuthentication(brokenToken));
    }

    @Test
    @DisplayName("JWT 구조는 맞지만 payload가 없는 경우 InvalidTokenException 발생")
    void tokenWithNoPayloadShouldThrowException() {
        String fakeToken = "abc.def.ghi";

        assertThrows(InvalidTokenException.class,
                () -> jwtTokenProvider.validateToken(fakeToken));
    }
}
