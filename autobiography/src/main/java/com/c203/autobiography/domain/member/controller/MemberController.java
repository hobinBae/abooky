package com.c203.autobiography.domain.member.controller;

import com.c203.autobiography.domain.group.dto.GroupApplyResponse;
import com.c203.autobiography.domain.group.dto.GroupResponse;
import com.c203.autobiography.domain.group.service.GroupApplyService;
import com.c203.autobiography.domain.group.service.GroupMemberService;
import com.c203.autobiography.domain.member.dto.MemberUpdateRequest;
import com.c203.autobiography.domain.member.dto.RepresentBookRequest;
import com.c203.autobiography.domain.member.dto.RepresentBookResponse;
import com.c203.autobiography.domain.member.service.MemberService;
import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.s3.FileStorageService;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "회원 API", description = "회원가입 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final GroupApplyService groupApplyService;
    private final GroupMemberService groupMemberService;

    @Operation(summary = "회원가입", description = "회원가입 + 프로필 사진 등록")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(
            @ModelAttribute @Valid MemberCreateRequest request,
            @RequestPart(value = "file", required = false)
            MultipartFile file, HttpServletRequest httpRequest
    ) {

        MemberResponse response = memberService.register(request, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원가입 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 회원 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest httpRequest) {
        MemberResponse response = memberService.getMyInfo(userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원 정보 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 회원 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails, @ModelAttribute @Valid MemberUpdateRequest request,@RequestPart(value = "file", required = false)
    MultipartFile file, HttpServletRequest httpRequest) {

        MemberResponse response = memberService.updateMyInfo(userDetails.getMemberId(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원 정보 수정 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자가 회원 탈퇴(soft delete)합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest httpRequest){
        memberService.deleteMyAccount(userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원 탈퇴가 완료되었습니다.", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "회원 상세 조회", description = " 회원의 상세 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable Long memberId, HttpServletRequest httpRequest) {
        MemberResponse response = memberService.getMemberById(memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원 상세 조회 성공", response, httpRequest.getRequestURI()));

    }

    @Operation(summary = "내가 받은 초대 목록 조회", description = "그룹에 초대받은 목록을 조회합니다.")
    @GetMapping("/me/invites")
    public ResponseEntity<ApiResponse<List<GroupApplyResponse>>> getMyInvites(
            @AuthenticationPrincipal CustomUserDetails currentUser, HttpServletRequest httpRequest
    ) {
        Long currentUserId = currentUser.getMemberId();
        List<GroupApplyResponse> response = groupApplyService.listReceivedInvites(currentUserId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "내가 온 초대 목록 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "내가 속한 그룹 목록 조회", description = "속한 그룹 목록을 조회합니다.")
    @GetMapping("/me/groups")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails currentUser, HttpServletRequest httpRequest
    ) {
        List<GroupResponse> groups = groupMemberService.listMyGroups(currentUser.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "내가 속한 그룹 목록 조회 성공", groups, httpRequest.getRequestURI()));
    }

    @Operation(summary = "대표책 설정/해제", description = "해당 책을 대표책으로 설정하거나 해제합니다.")
    @PatchMapping("/me/books/{bookId}/represent")
    public ResponseEntity<ApiResponse<RepresentBookResponse>> setRepresentativeBook(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long bookId,
            @RequestBody @Valid RepresentBookRequest request,
            HttpServletRequest httpRequest
    ) {
        RepresentBookResponse res = memberService.setRepresentativeBook(
                user.getMemberId(), bookId, request.getIsRepresentative());

        String message = request.getIsRepresentative() ? "대표책이 설정되었습니다." : "대표책이 해제되었습니다.";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, message, res, httpRequest.getRequestURI()));
    }
}
