package com.c203.autobiography.domain.member.repository;

import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<Member> findByEmailAndName(String email, String name);
    Optional<Member> findByNameAndPhoneNumber(String name, String phoneNumber);
    Optional<Member> findByMemberIdAndDeletedAtIsNull(Long memberId);
    boolean existsByMemberIdAndDeletedAtIsNull(Long memberId);
}
