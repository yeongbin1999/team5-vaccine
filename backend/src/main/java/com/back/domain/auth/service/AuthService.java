package com.back.domain.auth.service;

import com.back.domain.auth.dto.LoginRequest;
import com.back.domain.auth.dto.SignupRequest;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.security.jwt.JwtTokens;
import com.back.global.security.jwt.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${cookie.secure}")
    private boolean secure;

    // 회원가입
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        validatePasswordPolicy(request.getPassword());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .build();

        userRepository.save(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public JwtTokens login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return tokenService.generateToken(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 정책 검증
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 동일할 수 없습니다.");
        }
        validatePasswordPolicy(newPassword);

        // 새 비밀번호 암호화 후 저장
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);

        // 기존 RefreshToken 무효화 → 강제 재로그인 유도
        tokenService.logout(userId);
    }

    // RefreshToken 재발급
    @Transactional(readOnly = true)
    public JwtTokens reissue(String refreshToken) {
        return tokenService.reissue(refreshToken);
    }

    // RefreshToken 쿠키 생성
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        Duration expiration = tokenService.getRefreshTokenExpiration();
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(secure)  // 환경별 설정 (로컬 false, 운영 true)
                .sameSite("Strict")
                .path("/")
                .maxAge(expiration)
                .build();
    }

    // RefreshToken 쿠키 삭제
    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)  // 즉시 만료
                .build();
    }

    //  비밀번호 검증 정책
    private void validatePasswordPolicy(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
    }
}
