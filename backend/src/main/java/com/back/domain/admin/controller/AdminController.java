package com.back.domain.admin.controller;

import com.back.domain.admin.dto.PageResponseDto;
import com.back.domain.admin.service.AdminService;
import com.back.domain.user.dto.UpdateUserRequest;
import com.back.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 - 사용자 목록을 조회합니다
     * @param pageable 페이지네이션 정보
     * @param search 검색어 (이메일 또는 이름)
     * @return 사용자 목록과 페이지 정보를 담은 응답
     */
    @GetMapping("/users")
    @Operation(summary = "관리자 - 사용자 목록 조회 (페이지네이션, 검색 가능)",
            description = "모든 사용자 목록을 조회합니다. 검색어(search)를 통해 이메일 또는 이름으로 필터링할 수 있습니다.")
    public ResponseEntity<PageResponseDto<UserResponse>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String search) {
        PageResponseDto<UserResponse> users = adminService.getAllUsers(pageable, search);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/users/{userId}")
    @Operation(summary = "관리자 - 특정 사용자 정보 조회",
            description = "특정 사용자 ID를 통해 상세 정보를 조회합니다.")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다.") Integer userId) {
        UserResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * 관리자 - 특정 사용자 ID의 정보를 수정합니다.
     *
     * @param userId 수정할 사용자 ID
     * @param request 수정할 사용자 정보 (이름, 주소, 전화번호)
     * @return 수정된 사용자 정보 응답
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "관리자 - 특정 사용자 정보 수정",
            description = "특정 사용자 ID의 정보를 수정합니다. 이름, 주소, 전화번호를 수정할 수 있습니다.")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Positive(message = "사용자 ID는 양수여야 합니다.") Integer userId,
            @RequestBody @Valid UpdateUserRequest request) {
        UserResponse updatedUser = adminService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * NoSuchElementException (예: 리소스를 찾을 수 없을 때) 예외를 처리합니다.
     * HTTP Status 404 Not Found를 반환합니다.
     *
     * @param ex 발생한 NoSuchElementException
     * @return 오류 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        errors.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    /**
     * IllegalArgumentException (예: 유효하지 않은 인자) 예외를 처리합니다.
     * HTTP Status 400 Bad Request를 반환합니다.
     *
     * @param ex 발생한 IllegalArgumentException
     * @return 오류 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        errors.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
