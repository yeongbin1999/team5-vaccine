package com.back.domain.auth.controller;

import com.back.domain.auth.dto.ChangePasswordRequest;
import com.back.domain.auth.dto.LoginRequest;
import com.back.domain.auth.dto.SignupRequest;
import com.back.domain.auth.service.AuthService;
import com.back.global.security.auth.CustomUserDetails;
import com.back.global.security.jwt.JwtTokens;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public ResponseEntity<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        JwtTokens tokens = authService.login(request);

        ResponseCookie refreshCookie = authService.createRefreshTokenCookie(tokens.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .build();
    }

    @PostMapping("/logout")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie deleteCookie = authService.createLogoutCookie();
        response.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        authService.changePassword(
                userDetails.getId(),
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok("비밀번호가 변경되었습니다. 다시 로그인 해주세요.");
    }

    @PostMapping("/reissue")
    @Transactional(readOnly = true)
    public ResponseEntity<Void> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                        HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        JwtTokens newTokens = authService.reissue(refreshToken);

        ResponseCookie refreshCookie = authService.createRefreshTokenCookie(newTokens.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newTokens.accessToken())
                .build();
    }
}
