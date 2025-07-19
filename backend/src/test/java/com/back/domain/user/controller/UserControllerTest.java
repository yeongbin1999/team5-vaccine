package com.back.domain.user.controller;

import com.back.domain.user.dto.UpdateUserRequest;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("내 정보 확인")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void t1_getMe() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("관리자"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.phone").value("010-3333-4444"))
                .andExpect(jsonPath("$.address").value("서울시 송파구"));
    }

    @Test
    @DisplayName("내 정보 수정")
    @WithUserDetails(value = "admin@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void t2_updateMe() throws Exception {
        // GIVEN: 수정할 요청 생성
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "새로운 관리자",
                "010-9999-0000",
                "서울시 강남구"
        );

        // WHEN: PATCH 요청 수행
        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새로운 관리자"))
                .andExpect(jsonPath("$.phone").value("010-9999-0000"))
                .andExpect(jsonPath("$.address").value("서울시 강남구"));

        // THEN: DB에 반영되었는지 검증
        User updatedUser = userRepository.findByEmail("admin@test.com")
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        assertThat(updatedUser.getName()).isEqualTo("새로운 관리자");
        assertThat(updatedUser.getPhone()).isEqualTo("010-9999-0000");
        assertThat(updatedUser.getAddress()).isEqualTo("서울시 강남구");
    }
}
