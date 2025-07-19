package com.back.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    @Qualifier("refreshTokenRotationScript")
    private final DefaultRedisScript<Long> rotateTokenScript;

    // userId를 key로, refreshToken을 value로 저장
    public void saveRefreshToken(Integer userId, String refreshToken, Duration expiration) {
        redisTemplate.opsForValue().set(String.valueOf(userId), refreshToken, expiration);
    }

    // userId로 저장된 refreshToken 조회
    public String getRefreshTokenByUserId(Integer userId) {
        return redisTemplate.opsForValue().get(String.valueOf(userId));
    }

    // userId로 저장된 refreshToken 삭제 (로그아웃, 강제 만료)
    public void deleteRefreshToken(Integer userId) {
        redisTemplate.delete(String.valueOf(userId));
    }

    public boolean rotateRefreshToken(Integer userId, String oldRefreshToken, String newRefreshToken, Duration expiration) {
        Long result = redisTemplate.execute(
                rotateTokenScript,
                Collections.singletonList(String.valueOf(userId)),
                oldRefreshToken,
                newRefreshToken,
                String.valueOf(expiration.toMillis())
        );

        return result != null && result == 1L;
    }
}
