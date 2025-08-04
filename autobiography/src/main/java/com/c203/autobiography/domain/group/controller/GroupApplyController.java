package com.c203.autobiography.domain.group.controller;

import com.c203.autobiography.domain.group.dto.GroupApplyRequest;
import com.c203.autobiography.domain.group.dto.GroupApplyResponse;
import com.c203.autobiography.domain.group.service.GroupApplyService;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/invites")
@RequiredArgsConstructor
@Validated
public class GroupApplyController {

    private final GroupApplyService groupApplyService;

    @GetMapping
    public ResponseEntity<List<GroupApplyResponse>> listInvites(@PathVariable Long groupId){
        return ResponseEntity.ok(groupApplyService.listInvites(groupId));

    }

    @PostMapping
    public ResponseEntity<GroupApplyResponse> invitedMember(
            @PathVariable Long groupId,
            @RequestBody @Valid GroupApplyRequest request
            ) {
        GroupApplyResponse response = groupApplyService.createInvite(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/invites")
    public ResponseEntity<List<GroupApplyResponse>> getMyInvites(
            @AuthenticationPrincipal CustomUserDetails currentUser
            ) {
        Long currentUserId = currentUser.getMemberId();
        List<GroupApplyResponse> response = groupApplyService.listReceivedInviteds(currentUserId);
        return ResponseEntity.ok(response);
    }
}
