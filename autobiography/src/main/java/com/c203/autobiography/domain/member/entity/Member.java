package com.c203.autobiography.domain.member.entity;

import com.c203.autobiography.domain.member.dto.AuthProvider;
import com.c203.autobiography.domain.member.dto.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.security.Provider;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    private LocalDate birthdate;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer coin; // int로 하면 null 허용이 안 되므로 Integer 사용

    @Column(length = 255)
    private String intro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ADMIN','MEMBER') DEFAULT 'MEMBER'")
    private Role role;

    /** 소셜 로그인 관련 필드 **/
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(length = 100)
    private String providerId;

    @Column(name = "represent_book_id")
    private Long representBookId;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 회원 정보 수정을 위한 메서드
     * @param nickname
     * @param phoneNumber
     * @param profileImageUrl
     * @param intro
     */
    public void updateInfo(String nickname, String phoneNumber, String profileImageUrl, String intro) {
        if (nickname != null) this.nickname = nickname;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (intro != null) this.intro = intro;
    }

    public void changePassword(String encodedPassword){
        this.password = encodedPassword;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
