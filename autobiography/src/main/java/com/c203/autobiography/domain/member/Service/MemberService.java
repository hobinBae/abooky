package com.c203.autobiography.domain.member.service;

import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;

public interface MemberService {
    public MemberResponse register(MemberCreateRequest request);
}
