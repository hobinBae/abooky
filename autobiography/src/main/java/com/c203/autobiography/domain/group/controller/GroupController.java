package com.c203.autobiography.domain.group.controller;

import com.c203.autobiography.domain.group.dto.GroupCreateRequest;
import com.c203.autobiography.domain.group.dto.GroupMemberResponse;
import com.c203.autobiography.domain.group.dto.GroupResponse;
import com.c203.autobiography.domain.group.dto.GroupUpdateRequest;
import com.c203.autobiography.domain.group.service.GroupMemberService;
import com.c203.autobiography.domain.group.service.GroupService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    @Operation(summary = "그룹 생성", description = "그룹 정보 + 커버 이미지 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @AuthenticationPrincipal CustomUserDetails currentUserId,
            @Valid @ModelAttribute GroupCreateRequest request,
            @RequestPart(value = "file", required = false)MultipartFile file,
            HttpServletRequest httpRequest
            ) {
        GroupResponse response = groupService.createGroup(currentUserId.getMemberId(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "그룹 생성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 조회", description = "그룹 아이디로 그룹 정보 조회합니다.")
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        GroupResponse response =  groupService.getGroup(groupId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 정보 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 정보 수정", description = "현재 로그인한 사용자가 그룹의 리더라면 그룹 정보를 수정합니다.")
    @PatchMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @AuthenticationPrincipal CustomUserDetails currentUserId,
            @PathVariable Long groupId,
            @Valid @ModelAttribute GroupUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile imageFile,
            HttpServletRequest httpRequest
            ) {
        GroupResponse response = groupService.updateGroup(currentUserId.getMemberId(), groupId, request, imageFile);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 정보 수정 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 삭제", description = "현재 로그인한 사용자가 그룹의 리더라면 그룹을 삭제(soft delete)합니다.")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails currentUserId,
            @PathVariable Long groupId, HttpServletRequest httpRequest) {
        groupService.deleteGroup(currentUserId.getMemberId(), groupId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.of(HttpStatus.NO_CONTENT, "그룹 삭제가 완료되었습니다.", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹원 조회", description = "현재 그룹의 그룹원들을 조회합니다.")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> listMembers(
            @PathVariable Long groupId,
            HttpServletRequest httpRequest
    ) {
        List<GroupMemberResponse> response = groupMemberService.listGroupMembers(groupId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹원 목록 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹원 강퇴", description = "현재 그룹에서 그룹원을 강퇴시킵니다.")
    @DeleteMapping("/{groupId}/{targetId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @PathVariable Long groupId,
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest
    ) {
        groupMemberService.removeMember(groupId, targetId, currentUser.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹원 강퇴 성공", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 탈퇴", description = "현재 그룹을 탈퇴합니다.")
    @DeleteMapping("/{groupId}/me")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest
    ) {
        groupMemberService.leaveGroup(groupId, currentUser.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 탈퇴 성공", null, httpRequest.getRequestURI()));

    }

}
