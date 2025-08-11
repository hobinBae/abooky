package com.c203.autobiography.domain.groupbook.episode;

import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeCreateRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextResponse;
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

@Tag(name = "GroupEpisode")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/books/{groupBookId}/episodes")
@RequiredArgsConstructor
public class GroupEpisodeController {

    private final GroupEpisodeService service;

    @Operation(summary = "에피소드 생성/시작")
    @PostMapping
    public ResponseEntity<ApiResponse<EpisodeResponse>> create(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody EpisodeCreateRequest req,
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
    public ResponseEntity<ApiResponse<EpisodeResponse>> get(
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
    public ResponseEntity<ApiResponse<EpisodeResponse>> finalizeEpisode(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            HttpServletRequest httpRequest
    ) {
        var res = service.finalizeEpisode(groupId, groupBookId, episodeId);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "완료", res, httpRequest.getRequestURI()));
    }
}
