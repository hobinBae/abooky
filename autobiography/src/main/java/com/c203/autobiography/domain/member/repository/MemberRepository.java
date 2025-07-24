package com.c203.autobiography.domain.member.repository;

import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /** 이메일로 회원 조회 */
    Optional<Member> findByEmail(String email);

    /** 닉네임 중복 확인용 */
    boolean existsByNickname(String nickname);

    /** 이메일 중복 확인용 */
    boolean existsByEmail(String email);
}
