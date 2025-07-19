package com.back.global.security.jwt;

import com.back.domain.user.entity.Role;
import com.back.global.exception.InvalidTokenException;
import com.back.global.exception.TokenExpiredException;
import com.back.global.security.auth.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final Key secretKey;
    @Getter
    private final Duration accessTokenExpiration;   // Duration 타입
    @Getter
    private final Duration refreshTokenExpiration;  // Duration 타입

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") Duration accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") Duration refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // Access Token 생성
    public String generateAccessToken(Integer id, String name, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration.toMillis());

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("name", name)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Integer userId) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Access 토큰에서 userId 추출
    public Integer getUserIdFromAccessToken(String token) {
        return Integer.valueOf(parseClaims(token).getSubject());
    }

    // Refresh 토큰에서 userId 추출
    public Integer getUserIdFromRefreshToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userId", Integer.class);
    }

    // 토큰 유효성 검증 및 예외 처리
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
    }

    // Claims 파싱 (내부)
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Access Token으로 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Integer id = Integer.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String roleStr = claims.get("role", String.class);
        Role role = Role.valueOf(roleStr);
        String authority = role.getKey();

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

        CustomUserDetails userDetails = new CustomUserDetails(
                id,
                email,
                "", // 비밀번호는 포함하지 않음
                name,
                role,
                authorities
        );

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    // 모든 Claims 반환 (RefreshToken type 검증 등에 사용)
    public Claims getAllClaims(String token) {
        return parseClaims(token);
    }
}
