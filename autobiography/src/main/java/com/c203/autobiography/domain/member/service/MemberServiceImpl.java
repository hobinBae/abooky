package com.c203.autobiography.domain.member.service;

import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.domain.member.service.MemberService;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
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
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. 닉네임 중복 검사
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }
}
