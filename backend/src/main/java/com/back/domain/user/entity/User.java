package com.back.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;   // ✅ DB가 INT면 Integer가 더 일치

    @Column(length = 20)
    private String name;  // ✅ 이름: 20자면 충분

    @Column(length = 100, unique = true, nullable = false)
    private String email; // ✅ 이메일: 최대 100자

    @Column(length = 100, nullable = false)
    private String password; // ✅ BCrypt 해시 기준 60자, 여유 있게 100자

    @Column(length = 200)
    private String address; // ✅ 도로명 + 상세주소 → 200자

    @Column(length = 20)
    private String phone;   // ✅ 국제번호 포함 → 20자면 충분

    @CreatedDate
    @Column(name = "join_date", updatable = false)
    private LocalDateTime joinDate;

    @LastModifiedDate
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;  // ✅ USER / ADMIN → 10자면 충분

    // ===== 편의 메서드 =====
    public void updateProfile(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}
