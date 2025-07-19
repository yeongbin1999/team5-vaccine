package com.back.global.security.auth;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getName(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
