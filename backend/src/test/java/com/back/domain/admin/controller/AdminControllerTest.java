package com.back.domain.admin.controller;

import com.back.domain.user.dto.UpdateUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // data-test.sql이 이 프로파일에서 로드됩니다.
@Transactional // 각 테스트 후 데이터베이스 롤백
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateUserRequest validUpdateRequest;
    private UpdateUserRequest invalidUpdateRequest;

    // data-test.sql에 정의된 사용자 ID를 상수로 정의하여 테스트에서 사용
    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer USER1_ID = 2;
    private static final String USER1_EMAIL = "user1@test.com";

    @BeforeEach
    void setUp() {
        // 유효한 사용자 업데이트 요청 DTO
        validUpdateRequest = new UpdateUserRequest(
                "수정된 유저1 이름",
                "010-1234-5678",
                "서울시 강남구 수정동"
        );

        // UpdateUserRequest에 @NotBlank가 추가되었으므로,
        // name 필드를 빈 문자열로 보내면 유효성 검증에 실패합니다.
        invalidUpdateRequest = new UpdateUserRequest(
                "", // @NotBlank 어노테이션으로 인해 유효성 검증 실패 예상
                "010-1111-2222",
                "유효하지 않은 주소"
        );
    }

    // --- 사용자 목록 조회 테스트 ---

    @Test
    @DisplayName("GET /api/v1/admin/users - 사용자 목록 조회 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].id").value(ADMIN_USER_ID))
                .andExpect(jsonPath("$.content[1].id").value(USER1_ID));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - 사용자 목록 검색 조회 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Search_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .param("search", USER1_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value(USER1_EMAIL));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - 사용자 목록 조회 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void getAllUsers_Forbidden_WithUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/users - 사용자 목록 조회 실패 (인증 없음)")
    void getAllUsers_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // --- 특정 사용자 정보 조회 테스트 ---

    @Test
    @DisplayName("GET /api/v1/admin/users/{userId} - 특정 사용자 정보 조회 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success_WithAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", USER1_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER1_ID))
                .andExpect(jsonPath("$.email").value(USER1_EMAIL))
                .andExpect(jsonPath("$.name").value("유저1"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{userId} - 특정 사용자 정보 조회 실패 (사용자 없음)")
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound_WithAdmin() throws Exception {
        Integer nonExistingUserId = 9999;

        mockMvc.perform(get("/api/v1/admin/users/{userId}", nonExistingUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{userId} - 특정 사용자 정보 조회 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void getUserById_Forbidden_WithUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", ADMIN_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{userId} - 특정 사용자 정보 조회 실패 (인증 없음)")
    void getUserById_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", USER1_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{userId} - 특정 사용자 정보 조회 실패 (유효하지 않은 ID)")
    @WithMockUser(roles = "ADMIN")
    void getUserById_BadRequest_InvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}", -1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // --- 특정 사용자 정보 수정 테스트 ---

    @Test
    @DisplayName("PUT /api/v1/admin/users/{userId} - 특정 사용자 정보 수정 성공 (관리자 권한)")
    @WithMockUser(roles = "ADMIN")
    void updateUser_Success_WithAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/{userId}", USER1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER1_ID))
                .andExpect(jsonPath("$.name").value(validUpdateRequest.name()))
                .andExpect(jsonPath("$.address").value(validUpdateRequest.address()))
                .andExpect(jsonPath("$.phone").value(validUpdateRequest.phone()));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/users/{userId} - 특정 사용자 정보 수정 실패 (사용자 없음)")
    @WithMockUser(roles = "ADMIN")
    void updateUser_NotFound_WithAdmin() throws Exception {
        Integer nonExistingUserId = 9999;

        mockMvc.perform(put("/api/v1/admin/users/{userId}", nonExistingUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value("404"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/users/{userId} - 특정 사용자 정보 수정 실패 (일반 사용자)")
    @WithMockUser(roles = "USER")
    void updateUser_Forbidden_WithUser() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/{userId}", ADMIN_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/users/{userId} - 특정 사용자 정보 수정 실패 (인증 없음)")
    void updateUser_Unauthorized_WithoutAuth() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/{userId}", USER1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/users/{userId} - 특정 사용자 정보 수정 실패 (유효성 검증 실패)")
    @WithMockUser(roles = "ADMIN")
    void updateUser_BadRequest_InvalidRequest() throws Exception {
        // @NotBlank 어노테이션 추가로 인해 빈 문자열은 유효성 검증 실패
        mockMvc.perform(put("/api/v1/admin/users/{userId}", USER1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 400 Bad Request 예상
    }

    @Test
    @DisplayName("PUT /api/v1/admin/users/{userId} - 특정 사용자 정보 수정 실패 (유효하지 않은 사용자 ID)")
    @WithMockUser(roles = "ADMIN")
    void updateUser_BadRequest_InvalidUserId() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/{userId}", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}