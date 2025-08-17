package com.c203.autobiography.domain.group.controller;

import com.c203.autobiography.domain.group.dto.GroupApplyRequest;
import com.c203.autobiography.domain.group.dto.GroupApplyResponse;
import com.c203.autobiography.domain.group.dto.GroupApplyStatusRequest;
import com.c203.autobiography.domain.group.service.GroupApplyService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "그룹 초대 API", description = "GroupApply 관련 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/invites")
@RequiredArgsConstructor
@Validated
public class GroupApplyController {

    private final GroupApplyService groupApplyService;

    @Operation(summary = "그룹 초대 목록 조회", description = "그룹이 보낸 초대 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupApplyResponse>>> listInvites(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        List<GroupApplyResponse> response = groupApplyService.listInvites(groupId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 초대 목록 조회 성공", response, httpRequest.getRequestURI()));

    }

    @Operation(summary = "그룹원 초대하기", description = "이메일로 그룹에 초대할 수 있습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<GroupApplyResponse>> invitedMember(
            @PathVariable Long groupId,
            @RequestBody @Valid GroupApplyRequest request,
            HttpServletRequest httpRequest
            ) {
        GroupApplyResponse response = groupApplyService.createInvite(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "그룹원 초대 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 초대 수락/거절", description = "그룹 초대에 대한 상태를 변경합니다. ACCEPTED : 수락 / DENIED : 거절")
    @PatchMapping("/{groupApplyId}")
    public ResponseEntity<ApiResponse<GroupApplyResponse>> handleInvite(
            @PathVariable("groupId") Long groupId,
            @PathVariable("groupApplyId") Long groupApplyId,
            @RequestBody @Valid GroupApplyStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest
    ) {
        GroupApplyResponse updated = groupApplyService.updateInviteStatus(
                groupId, groupApplyId, request.getStatus(),
                currentUser.getMemberId()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "초대 " + request.getStatus().name().toLowerCase() + " 처리 성공", updated, httpRequest.getRequestURI()));
    }


}
