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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "GroupEpisode")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/books/{groupBookId}/episodes")
@RequiredArgsConstructor
public class GroupEpisodeController {

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
        var res = groupEpisodeService.create(groupId, groupBookId, req, user.getMemberId());
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
        var res = groupEpisodeService.next(groupId, groupBookId, episodeId, req, user.getMemberId());
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
        var res = groupEpisodeService.get(groupId, groupBookId, episodeId);
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
        var res = groupEpisodeService.finalizeEpisode(groupId, groupBookId, episodeId);
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
            @Valid @ModelAttribute GroupEpisodeUpdateRequest req,
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

    // ========== 이미지 관리 API ==========

    @Operation(summary = "에피소드 이미지 업로드", description = "그룹 에피소드에 이미지를 업로드합니다.")
    @PostMapping(value = "/{episodeId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GroupEpisodeImageResponse>> uploadImage(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "orderNo", required = false) Integer orderNo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        // Request 객체 생성
        GroupEpisodeImageUploadRequest request = GroupEpisodeImageUploadRequest.builder()
                .description(description)
                .orderNo(orderNo)
                .build();
        
        GroupEpisodeImageResponse response = groupEpisodeService.uploadImage(
                groupId, groupBookId, episodeId, file, request, userDetails.getMemberId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.of(HttpStatus.CREATED, "이미지 업로드 성공", response, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "에피소드 이미지 목록 조회", description = "그룹 에피소드의 모든 이미지를 조회합니다.")
    @GetMapping("/{episodeId}/images")
    public ResponseEntity<ApiResponse<List<GroupEpisodeImageResponse>>> getImages(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        List<GroupEpisodeImageResponse> response = groupEpisodeService.getImages(
                groupId, groupBookId, episodeId, userDetails.getMemberId()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.of(HttpStatus.OK, "이미지 목록 조회 성공", response, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "에피소드 이미지 삭제", description = "그룹 에피소드의 특정 이미지를 삭제합니다.")
    @DeleteMapping("/{episodeId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        groupEpisodeService.deleteImage(groupId, groupBookId, episodeId, imageId, userDetails.getMemberId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.of(HttpStatus.NO_CONTENT, "이미지 삭제 성공", null, httpRequest.getRequestURI())
        );
    }

}
