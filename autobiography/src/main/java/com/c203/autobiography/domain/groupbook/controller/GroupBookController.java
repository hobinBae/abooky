package com.c203.autobiography.domain.groupbook.controller;

import com.c203.autobiography.domain.groupbook.dto.*;
import com.c203.autobiography.domain.groupbook.episode.dto.GroupAnswerCorrectionRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.GroupAnswerCorrectionResponse;
import com.c203.autobiography.domain.groupbook.episode.dto.GroupEpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.service.GroupEpisodeService;
import com.c203.autobiography.domain.groupbook.service.GroupBookService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "그룹책 API", description = "책, 에피소드 관련 API")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/books")
@RequiredArgsConstructor
public class GroupBookController {

    private final GroupBookService groupBookService;
    private final GroupEpisodeService episodeService;

    @Operation(summary = "그룹 책 생성", description = "그룹 책 정보만 등록합니다. (에피소드는 별도 생성)")
    @PostMapping
    public ResponseEntity<ApiResponse<GroupBookCreateResponse>> createBook(
            @AuthenticationPrincipal CustomUserDetails userDetail,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupBookCreateRequest request,
            HttpServletRequest httpRequest
            ) {
        Long memberId = userDetail.getMemberId();
        GroupBookCreateResponse response = groupBookService.createBookSimple(groupId, memberId, request, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "그룹책 생성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹책 완성", description = "그룹 책을 완성합니다.")
    @PatchMapping("{groupBookId}/completed")
    public ResponseEntity<ApiResponse<GroupBookResponse>> completeBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @Valid @RequestBody GroupBookCompleteRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupBookResponse response = groupBookService.completeBook(groupId, memberId, groupBookId, request.getTags());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 책 완성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 책 조회", description = "그룹 책을 조회합니다.")
    @GetMapping("/{groupBookId}")
    public ResponseEntity<ApiResponse<GroupBookResponse>> getBookDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            HttpServletRequest httpRequest

    ) {
        Long memberId = userDetails.getMemberId();
        GroupBookResponse response = groupBookService.getBookDetail(groupId, memberId, groupBookId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹책 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹책 목록 조회", description = "그룹에서 쓴 책을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupBookResponse>>> getGroupBooks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        List<GroupBookResponse> response = groupBookService.getMyGroupBooks(groupId, memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 보유 책 목록 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹책 수정", description = "그룹책을 수정합니다.")
    @PatchMapping("/{groupBookId}")
    public ResponseEntity<ApiResponse<GroupBookResponse>> updateBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @Valid @ModelAttribute GroupBookUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupBookResponse response = groupBookService.updateBook(groupId, memberId, groupBookId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "그룹책 수정 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹책 삭제", description = "그룹책을 삭제합니다")
    @DeleteMapping("/{groupBookId}")
    public ResponseEntity<ApiResponse<GroupBookResponse>> deleteBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        groupBookService.deleteBook(groupId, memberId, groupBookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.of(HttpStatus.NO_CONTENT, "그룹책 삭제 성공", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 책 댓글 생성", description = "그룹 책에 대한 댓글을 생성합니다")
    @PostMapping("/{groupBookId}/comments")
    public ResponseEntity<ApiResponse<GroupBookCommentCreateResponse>> createGroupBookComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GroupBookCommentCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupBookCommentCreateResponse response= groupBookService.createGroupBookComment(memberId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 책 댓글 생성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 책 댓글 목록 조회", description = "특정 그룹 책의 댓글 목록을 조회합니다")
    @GetMapping("/{groupBookId}/comments")
    public ResponseEntity<ApiResponse<GroupBookCommentListResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "커뮤니티 책 ID") @PathVariable Long groupBookId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupBookCommentListResponse response = groupBookService.getGroupBookComments(memberId, groupBookId, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 책 댓글 리스트 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "그룹 책 댓글 삭제", description = "그룹 책에 대한 댓글을 생성합니다")
    @DeleteMapping("/{groupBookId}/comments/{groupBookCommentId}")
    public ResponseEntity<ApiResponse<GroupBookCommentDeleteResponse>> deleteGroupBookComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "그룹 책 ID") @PathVariable Long groupBookId,
            @Parameter(description = "그룹 책 댓글 ID") @PathVariable Long groupBookCommentId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupBookCommentDeleteResponse response= groupBookService.deleteGroupBookComment(groupBookId, groupBookCommentId, memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "그룹 책 댓글 삭제 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "다음 템플릿 에피소드 생성", description = "현재 템플릿 완료 후 다음 템플릿의 새로운 에피소드를 생성합니다.")
    @PostMapping("/{groupBookId}/episodes/next-template")
    public ResponseEntity<ApiResponse<GroupEpisodeResponse>> createNextTemplateEpisode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @RequestParam String currentTemplate,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupEpisodeResponse response = episodeService.createNextTemplateEpisode(
                groupId, groupBookId, currentTemplate, memberId
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "다음 템플릿 에피소드 생성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "답변 교정", description = "질문과 답변을 바탕으로 AI가 답변을 교정합니다.")
    @PostMapping("/{groupBookId}/episodes/{episodeId}/correct-answer")
    public ResponseEntity<ApiResponse<GroupAnswerCorrectionResponse>> correctAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long groupBookId,
            @PathVariable Long episodeId,
            @Valid @RequestBody GroupAnswerCorrectionRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        GroupAnswerCorrectionResponse response = episodeService.correctAnswer(
                groupId, groupBookId, episodeId, request, memberId
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "답변 교정 성공", response, httpRequest.getRequestURI()));
    }


}
