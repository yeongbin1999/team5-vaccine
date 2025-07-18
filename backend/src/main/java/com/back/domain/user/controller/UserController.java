package com.back.domain.user.controller;

import com.back.domain.user.dto.UserResponse;
import com.back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMe() {
        Integer dummyUserId = 1;
        UserResponse response = userService.getCurrentUser(dummyUserId);
        return ResponseEntity.ok(response);
    }
}
