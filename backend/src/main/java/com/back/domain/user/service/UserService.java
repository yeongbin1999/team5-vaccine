package com.back.domain.user.service;

import com.back.domain.user.dto.UserResponse;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(Integer userId) {
        return userRepository.findById(userId)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getAddress()
                ))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
