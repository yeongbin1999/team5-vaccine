package com.back.domain.user.controller;

import com.back.domain.user.dto.UpdateUserRequest;
import com.back.domain.user.dto.UserResponse;
import com.back.domain.user.service.UserService;
import com.back.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Integer userId = customUserDetails.getId();
        UserResponse response = userService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody UpdateUserRequest request
    ) {
        Integer userId = customUserDetails.getId();
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }
}
