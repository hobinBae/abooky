package com.c203.autobiography.domain.communityBook.controller;

import com.c203.autobiography.domain.book.dto.LikeResponse;
import com.c203.autobiography.domain.communityBook.dto.*;
import com.c203.autobiography.domain.communityBook.service.CommunityBookService;
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

import java.util.Optional;

@Tag(name="커뮤니티 책 API", description = "커뮤니티 책 관련 API")
@RestController
@RequestMapping("/api/v1/communities/community-book")
@RequiredArgsConstructor
public class CommunityBookController {

    private final CommunityBookService communityBookService;

    @Operation(summary = "커뮤니티 책 조회(읽기)", description = "커뮤니티 책을 상세 조회합니다.(첵 읽기) ")
    @GetMapping("/detail/{communityBookId}")
    public ResponseEntity<ApiResponse<CommunityBookDetailResponse>> getCommunityBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long communityBookId,
            HttpServletRequest httpRequest) {
        Long memberId = userDetails.getMemberId();
        CommunityBookDetailResponse response = communityBookService.getCommunityBookDetail(memberId, communityBookId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 조회(읽기) 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 목록 조회", description = "커뮤니티 책 목록을 조회합니다")
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<CommunityBookListResponse>> getCommunityBooks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,

            @RequestParam(required = false, defaultValue = "recent") String sortBy) {
        Long memberId = userDetails.getMemberId();
        CommunityBookListResponse response = communityBookService.getCommunityBookList(
                memberId, pageable, sortBy);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 목록 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 검색", description = "커뮤니티 책을 검색합니다")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CommunityBookListResponse>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,

            @Parameter(description = "책 제목 키워드 검색")
            @RequestParam(required = false) String title,

            @Parameter(description = "태그명으로 검색 (쉼표로 구분)")
            @RequestParam(required = false) String[] tags,

            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String bookType,
            @RequestParam(required = false, defaultValue = "recent") String sortBy) {
        Long memberId = userDetails.getMemberId();
        CommunityBookListResponse response = communityBookService.search(
                memberId, pageable, title, tags, categoryId, bookType, sortBy);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 검색 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "특정 작가가 작성한 커뮤니티 책 목록 조회", description = "특정 작가가 작성한 커뮤니티 책 목록을 조회합니다")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<CommunityBookListResponse>> getMemberCommunityBooks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberId,
            HttpServletRequest httpRequest,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String bookType,
            @RequestParam(required = false, defaultValue = "recent") String sortBy) {
        Long userId = userDetails.getMemberId();
        CommunityBookListResponse response = communityBookService.getMemberCommunityBooks(
                userId, memberId, pageable, categoryId, bookType, sortBy);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "특정 작가가 작성한 커뮤니티 책 목록 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 삭제", description = "커뮤니티 책을 삭제합니다")
    @DeleteMapping("/books/{communityBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteCommunityBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long communityBookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        communityBookService.deleteCommunityBook(memberId, communityBookId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 삭제 성공", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 댓글 생성", description = "커뮤니티 책에 대한 댓글을 생성합니다")
    @PostMapping("/{communityBookId}/comments")
    public ResponseEntity<ApiResponse<CommunityBookCommentCreateResponse>> createCommunityBookComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommunityBookCommentCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        CommunityBookCommentCreateResponse response= communityBookService.createCommunityBookComment(memberId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 댓글 생성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 댓글 목록 조회", description = "특정 커뮤니티 책의 댓글 목록을 조회합니다")
    @GetMapping("/{communityBookId}/comments")
    public ResponseEntity<ApiResponse<CommunityBookCommentListResponse>> getComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "커뮤니티 책 ID") @PathVariable Long communityBookId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        CommunityBookCommentListResponse response = communityBookService.getCommunityCookComments(memberId, communityBookId, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 댓글 리스트 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 댓글 삭제", description = "커뮤니티 책에 대한 댓글을 생성합니다")
    @DeleteMapping("/{communityBookId}/comments/{communityBookCommentId}")
    public ResponseEntity<ApiResponse<CommunityBookCommentDeleteResponse>> deleteCommunityBookComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "커뮤니티 책 ID") @PathVariable Long communityBookId,
            @Parameter(description = "커뮤니티 책 댓글 ID") @PathVariable Long communityBookCommentId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        CommunityBookCommentDeleteResponse response= communityBookService.deleteCommunityBookComment(communityBookId, communityBookCommentId, memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 댓글 삭제 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 좋아요/좋아요 취소", description = "커뮤니티 책에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/{communityBookId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> likeBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long communityBookId,
            HttpServletRequest httpRequest

    ) {
        Long memberId = userDetails.getMemberId();
        boolean isLiked = communityBookService.toggleLike(communityBookId, memberId);

        String message = isLiked ? "커뮤니티 책 좋아요 성공" : "커뮤니티 책 좋아요 취소 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.CREATED, message, null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 북 좋아요 수 조회", description = "해당 커뮤니티 북의 총 좋아요 수를 조회합니다")
    @GetMapping("/{communityBookId}/likes/count")
    public ResponseEntity<ApiResponse<CommunityBookLikeCntResponse>> getLikeCount(
            @Parameter(description = "커뮤니티 북 ID", required = true)
            @PathVariable Long communityBookId,
            HttpServletRequest httpRequest) {

        long likeCount = communityBookService.getLikeCount(communityBookId);

        CommunityBookLikeCntResponse response = CommunityBookLikeCntResponse.of(communityBookId, likeCount);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.CREATED, "커뮤니티 북 좋아요 수 조회 성공", response, httpRequest.getRequestURI()));
    }


    @Operation(summary = "커뮤니티 북 북마크/북마크 취소", description = "커뮤니티 북 북마크를 토글합니다 (북마크가 있으면 취소, 없으면 추가)")
    @PostMapping("/{communityBookId}/bookmark")
    public ResponseEntity<ApiResponse<Object>> toggleBookmark(
            @Parameter(description = "커뮤니티 북 ID", required = true)
            @PathVariable Long communityBookId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {

        Long memberId = userDetails.getMemberId();
        boolean isBookmarked = communityBookService.toggleBookmark(communityBookId, memberId);

        String message = isBookmarked ? "커뮤니티 책 북마크 성공" : "커뮤니티 책 북마크 취소 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, message, null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 평점 생성", description = "커뮤니티 책에 평점을 생성합니다")
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<CommunityBookRatingResponse>> createOrUpdateRating(
            @Valid @RequestBody CommunityBookRatingRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest) {

        Long memberId = userDetails.getMemberId();
        CommunityBookRatingResponse response = communityBookService.createRating(memberId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "평점이 등록되었습니다.", response, httpRequest.getRequestURI()));
    }


}
