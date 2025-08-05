package com.c203.autobiography.domain.rtc.controller;

import com.c203.autobiography.domain.group.service.GroupMemberService;
import com.c203.autobiography.domain.group.service.GroupService;
import com.c203.autobiography.domain.rtc.dto.RtcTokenResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/rtc")
@RequiredArgsConstructor
public class GroupRtcController {
    private final LivekitService livekitService;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    // 그룹 전용 방 생성
    @PostMapping("/room")
    public ResponseEntity<Void> createGroupRoom(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails currentUser
            ) throws Exception {
        // 그룹 소속 확인하기
        groupMemberService.verifyMember(groupId, currentUser.getMemberId());

        // RoomName 설정
        String roomName = "group-" + groupId;

        // 방 생성 시도 (있으면 예외)
        try {
            livekitService.ensureRoomExists(roomName);
        } catch (RoomAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Created + Location 헤더
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/room")
                .build()
                .toUri();
        return ResponseEntity.created(location).build();

    }

    @GetMapping("/token")
    public ResponseEntity<RtcTokenResponse> getGroupToken(
            @PathVariable Long groupId,
            @RequestBody RtcRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws Exception {
        groupMemberService.verifyMember(groupId, currentUser.getMemberId());
        String roomName = "group" + groupId;

        // 방이 없으면 생성하기 (idempotent)
        livekitService.ensureRoomExists(roomName);

        String token = livekitService.createJoinToken(
                roomName, currentUser.getMemberId(), request.getMemberId()
        );

        String url = "ws://localhost:7881";
        return ResponseEntity.ok(new RtcTokenResponse(url, token));
    }

    @DeleteMapping("/room")
    public ResponseEntity<Void> deleteGroupRoom(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws Exception {
        groupMemberService.verifyMember(groupId, currentUser.getMemberId());
        rct
    }
}
