package com.c203.autobiography.domain.member.Service;

import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;

public interface MemberService {
    public MemberResponse register(MemberCreateRequest request);
}
