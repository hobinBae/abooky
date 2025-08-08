package com.c203.autobiography.domain.rtc.controller;

import com.c203.autobiography.domain.group.service.GroupMemberService;
import com.c203.autobiography.domain.rtc.dto.RtcRequest;
import com.c203.autobiography.domain.rtc.dto.RtcTokenResponse;
import com.c203.autobiography.domain.rtc.service.GroupRoomService;
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
import org.springframework.web.bind.annotation.*;


@Tag(name = "그룹 화상채팅방 API", description = "WebRTC 관련 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/rtc")
@RequiredArgsConstructor
public class GroupRtcController {

    private final GroupRoomService groupRoomService;
    private final GroupMemberService groupMemberService;

    @Operation(summary = "그룹 화상 채팅 방 생성", description = "그룹 멤버만 방을 생성할 수 있습니다.")
    @PostMapping("/room")
    public ResponseEntity<ApiResponse<Void>> createGroupRoom(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest
            ) throws Exception {
        // 그룹 소속 확인하기
        groupMemberService.verifyMember(groupId, currentUser.getMemberId());

        groupRoomService.createRoom(groupId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "그룹 화상 채팅방 생성 성공", null, httpRequest.getRequestURI()));

    }

    @Operation(summary = "그룹 방 입장 토큰 발급", description = "그룹 멤버에게 방 입장 토큰을 발급합니다.")
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<RtcTokenResponse>> getGroupToken(
            @PathVariable Long groupId,
            @RequestBody @Valid RtcRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest
    ) throws Exception {
        Long memberId = currentUser.getMemberId();
        groupMemberService.verifyMember(groupId, memberId);

        String token = groupRoomService.generateToken(groupId, memberId.toString(), request.getUserName());

        String url = "ws://localhost:7880";
        RtcTokenResponse response = new RtcTokenResponse(url, token);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "토큰 발급 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 화상 채팅 방 삭제", description = "그룹 멤버만 방을 삭제할 수 있습니다.")
    @DeleteMapping("/room")
    public ResponseEntity<ApiResponse<Void>> deleteGroupRoom(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest
    ) throws Exception {
        groupMemberService.verifyMember(groupId, currentUser.getMemberId());
        groupRoomService.deleteRoom(groupId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.of(HttpStatus.NO_CONTENT, "그룹 화상 채팅 방 삭제 성공", null, httpRequest.getRequestURI()));
    }
}
