package com.back.domain.auth.controller;

import com.back.domain.auth.dto.LoginRequest;
import com.back.domain.auth.dto.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test1234@example.com";
    private static final String TEST_PASSWORD = "password1234";
    private static final String TEST_NAME = "테스트유저1234";

    @Test
    @DisplayName("t1. 회원가입 성공")
    void t1_signup_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NAME))))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입 성공"));
    }

    @Test
    @DisplayName("t2. 로그인 성공 → 토큰과 refreshToken 쿠키 포함")
    void t2_login_success() throws Exception {
        // GIVEN: 회원가입 먼저 수행
        signupUser(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);

        // WHEN & THEN: 로그인 성공 시 accessToken + refreshToken 세팅
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        // refreshToken 쿠키가 비어있지 않은지 확인
        Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.getValue()).isNotBlank();
    }

    @Test
    @DisplayName("t3. 로그아웃 시 refreshToken 쿠키 삭제")
    void t3_logout_success() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andReturn();

        Cookie deletedCookie = result.getResponse().getCookie("refreshToken");
        assertThat(deletedCookie).isNotNull();
        assertThat(deletedCookie.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("t4. 리프레시 토큰 재발급 성공")
    void t4_reissue_success() throws Exception {
        // GIVEN: 회원가입 후 로그인
        signupUser(TEST_EMAIL, TEST_PASSWORD, TEST_NAME);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(TEST_EMAIL, TEST_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        // 로그인 응답에서 refreshToken 쿠키 추출
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isNotBlank();

        // WHEN & THEN: refreshToken으로 재발급 요청
        MvcResult reissueResult = mockMvc.perform(post("/api/v1/auth/reissue")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie newRefreshCookie = reissueResult.getResponse().getCookie("refreshToken");
        assertThat(newRefreshCookie).isNotNull();
        assertThat(newRefreshCookie.getValue()).isNotBlank();
    }

    private void signupUser(String email, String password, String name) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(email, password, name))))
                .andExpect(status().isOk());
    }
}
