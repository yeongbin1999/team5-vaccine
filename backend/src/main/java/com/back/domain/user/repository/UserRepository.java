package com.back.domain.user.repository;

import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    /**
     * 이메일에 특정 키워드가 포함된 사용자를 페이지네이션하여 조회합니다.
     */
    Page<User> findByEmail(String emailSearch, String nameSearch, Pageable pageable);
}
