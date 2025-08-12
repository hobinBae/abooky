package com.c203.autobiography.domain.groupbook.episode.controller;

import com.c203.autobiography.domain.groupbook.episode.dto.*;
import com.c203.autobiography.domain.groupbook.episode.service.GroupEpisodeService;
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

import java.util.List;

@Tag(name = "GroupEpisode")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/books/{groupBookId}/episodes")
@RequiredArgsConstructor
public class GroupEpisodeController {

    private final GroupEpisodeService service;
    private final GroupEpisodeService groupEpisodeService;

    @Operation(summary = "에피소드 생성/시작")
    @PostMapping
    public ResponseEntity<ApiResponse<GroupEpisodeResponse>> create(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody GroupEpisodeCreateRequest req,
            HttpServletRequest httpRequest
    ) {
        var res = service.create(groupId, groupBookId, req, user.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "에피소드 생성", res, httpRequest.getRequestURI()));
    }

    @Operation(summary = "다음 스텝 진행 (답변→편집→다음 질문)")
    @PostMapping("/{episodeId}/steps/next")
    public ResponseEntity<ApiResponse<StepNextResponse>> next(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody StepNextRequest req,
            HttpServletRequest httpRequest
    ) {
        var res = service.next(groupId, groupBookId, episodeId, req, user.getMemberId());
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK,"진행", res, httpRequest.getRequestURI()));
    }

    @Operation(summary = "에피소드 조회")
    @GetMapping("/{episodeId}")
    public ResponseEntity<ApiResponse<GroupEpisodeResponse>> get(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            HttpServletRequest httpRequest
    ) {
        var res = service.get(groupId, groupBookId, episodeId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "조회", res ,httpRequest.getRequestURI()));
    }

    @Operation(summary = "에피소드 확정")
    @PostMapping("/{episodeId}/finalize")
    public ResponseEntity<ApiResponse<GroupEpisodeResponse>> finalizeEpisode(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            HttpServletRequest httpRequest
    ) {
        var res = service.finalizeEpisode(groupId, groupBookId, episodeId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "완료", res, httpRequest.getRequestURI()));
    }


    @Operation(summary = "에피소드 목록 조회", description = "특정 그룹책의 모든 에피소드 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupEpisodeResponse>>> getEpisodeList(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        List<GroupEpisodeResponse> response = groupEpisodeService.getEpisodeList(
                groupId, groupBookId, userDetails.getMemberId()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.of(HttpStatus.OK, "그룹에피소드 목록 조회 성공", response, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "에피소드 수정", description = "그룹 에피소드의 제목, 순서 등을 수정합니다.")
    @PatchMapping("/{episodeId}")
    public ResponseEntity<ApiResponse<GroupEpisodeResponse>> updateEpisode(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GroupEpisodeUpdateRequest req,
            HttpServletRequest httpRequest
    ) {
        GroupEpisodeResponse response = groupEpisodeService.update(
                groupId, groupBookId, episodeId, req, userDetails.getMemberId()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.of(HttpStatus.OK, "그룹 에피소드 수정 성공", response, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "에피소드 삭제", description = "그룹 에피소드를 삭제합니다.")
    @DeleteMapping("/{episodeId}")
    public ResponseEntity<ApiResponse<GroupEpisodeResponse>> delete(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        groupEpisodeService.delete(groupId, groupBookId, episodeId, userDetails.getMemberId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.of(HttpStatus.NO_CONTENT, "그룹 에피소드 삭제 성공", null, httpRequest.getRequestURI())
        );
    }

}
