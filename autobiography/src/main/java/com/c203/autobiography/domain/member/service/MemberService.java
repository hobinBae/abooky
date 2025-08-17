package com.c203.autobiography.domain.member.service;

import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.domain.member.dto.MemberUpdateRequest;
import com.c203.autobiography.domain.member.dto.RepresentBookResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    public MemberResponse register(MemberCreateRequest request, MultipartFile file);
    public MemberResponse getMyInfo(Long memberId);
    public MemberResponse updateMyInfo(Long memberId, MemberUpdateRequest request, MultipartFile file);
    public void deleteMyAccount(Long memberId);
    public MemberResponse getMemberById(Long memberId);
    RepresentBookResponse setRepresentativeBook(Long memberId, Long bookId, boolean isRepresentative);
}
