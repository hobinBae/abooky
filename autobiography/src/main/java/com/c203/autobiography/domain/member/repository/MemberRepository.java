package com.c203.autobiography.domain.member.repository;

import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Member findByEmail(String email);
}
