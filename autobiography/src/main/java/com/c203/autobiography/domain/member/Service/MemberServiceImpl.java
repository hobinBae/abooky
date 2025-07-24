package com.c203.autobiography.domain.member.Service;

import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public MemberResponse register(MemberCreateRequest request) {
        //예외처리는 나중에 다시
        // 1. 이메일 중복 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 닉네임 중복 검사
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }
}
