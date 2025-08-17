package com.c203.autobiography.domain.member.service;

import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.domain.member.dto.MemberUpdateRequest;
import com.c203.autobiography.domain.member.dto.RepresentBookResponse;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.domain.member.service.MemberService;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final BookRepository bookRepository;

    private static final String DEFAULT_IMAGE_URL = "https://ssafytrip.s3.ap-northeast-2.amazonaws.com/userProfile/default.png";
    @Transactional
    @Override
    public MemberResponse register(MemberCreateRequest request, MultipartFile file) {
        //예외처리는 나중에 다시
        // 1. 이메일 중복 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. 닉네임 중복 검사
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        if (file != null && !file.isEmpty()) {
            String imageUrl = fileStorageService.store(file, "profiles");
            request.setProfileImageUrl(imageUrl);
        }else{
            request.setProfileImageUrl(DEFAULT_IMAGE_URL);
        }

        Member member = request.toEntity(passwordEncoder.encode(request.getPassword()));
        Member saved = memberRepository.save(member);
        return MemberResponse.from(saved);
    }

    @Override
    public MemberResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Transactional
    @Override
    public MemberResponse updateMyInfo(Long memberId, MemberUpdateRequest request, MultipartFile file) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        if (!member.getNickname().equals(request.getNickname())
                && memberRepository.existsByNickname(request.getNickname())) {
            throw new ApiException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        if (file != null && !file.isEmpty()) {

            // 2. 기존 이미지 URL이 있고, 디폴트 이미지가 아니라면 삭제
            String currentImageUrl = member.getProfileImageUrl(); // 또는 서비스에서 조회한 member 객체
            if (currentImageUrl != null && !currentImageUrl.isBlank()
                    && !currentImageUrl.equals(DEFAULT_IMAGE_URL)) {
                fileStorageService.delete(currentImageUrl);
            }

            // 3. 새 이미지 업로드
            String newImageUrl = fileStorageService.store(file, "profiles");

            // 4. DTO에 새 이미지 URL 반영
            request.setProfileImageUrl(newImageUrl);
        }
        member.updateInfo(request.getNickname(), request.getPhoneNumber(), request.getProfileImageUrl(), request.getIntro());
        log.info("Request nickname = {}", request.getNickname());
        log.info("Before update nickname = {}", member.getNickname());
        return MemberResponse.from(member);
    }

    @Transactional
    @Override
    public void deleteMyAccount(Long memberId) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        member.softDelete();
    }

    @Override
    public MemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Override
    @Transactional
    public RepresentBookResponse setRepresentativeBook(Long memberId, Long bookId, boolean isRepresentative) {
        // 1) 멤버 잠금 로드 (경합 방지)
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (isRepresentative) {
            // 2) 소유권/존재 검증
            boolean owned = bookRepository.existsByBookIdAndMember_MemberIdAndDeletedAtIsNull(bookId, memberId);
            if (!owned) {
                // 책이 없거나 내 소유가 아니면 거부
                throw new ApiException(ErrorCode.FORBIDDEN); // 필요 시 BOOK_NOT_OWNED 등 세분화
            }

            // (선택) 완성된 책만 허용하려면 여기에서 completed 체크 추가

            // 3) 대표책 설정 (기존 대표는 자동 교체)
            member.updateRepresentBookId(bookId);

        } else {
            // 4) 해제: 현재 대표가 이 bookId일 때만 null
            if (Objects.equals(member.getRepresentBookId(), bookId)) {
                member.updateRepresentBookId(null);
            }
            // 아니라면 no-op (원한다면 예외로 바꿔도 됨)
        }

        return RepresentBookResponse.builder()
                .memberId(member.getMemberId())
                .representBookId(member.getRepresentBookId()) // null이면 해제 상태
                .build();
    }



}
